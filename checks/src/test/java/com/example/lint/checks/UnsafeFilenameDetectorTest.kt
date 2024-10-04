package com.example.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UnsafeFilenameDetectorTest : LintDetectorTest() {
  override fun getIssues() = listOf(UnsafeFilenameDetector.ISSUE)

  override fun getDetector(): Detector = UnsafeFilenameDetector()

  @Test
  fun testDocumentationExample() {
    lint()
        .files(
            java(
                    """
                import android.database.Cursor;
                import android.provider.OpenableColumns;
                import java.io.File;

                class TestClass {
                  private void trustProvidedFilename_shouldWarn(Cursor cursor) {
                    int id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(id);

                    File fileObject = new File("./", fileName);
                  }
                }
            """)
                .indented(),
            kotlin(
                    """
                import android.database.Cursor
                import android.provider.OpenableColumns
                import java.io.File

                class TestClass {
                  private fun trustProvidedFilename_shouldWarn(cursor: Cursor) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(id)

                    val fileObject = File("./", fileName)
                  }
                }
                """)
                .indented(),
        )
        .run()
        .expect(
            """
            src/TestClass.java:10: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                File fileObject = new File("./", fileName);
                                                 ~~~~~~~~
            src/TestClass.kt:10: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                val fileObject = File("./", fileName)
                                            ~~~~~~~~
            0 errors, 2 warnings
            """)
  }

  @Test
  fun testUsingUnsanitizedFilenameAsVariable_shouldWarn() {
    lint()
        .files(
            java(
                    """
                import android.content.ContentResolver;
                import android.content.Context;
                import android.database.Cursor;
                import android.net.Uri;
                import android.provider.OpenableColumns;
                import java.io.File;

                class TestClass {
                  private void cursorFilenameAssignedToVariables(Cursor cursor) {
                    String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                    File fileObject = new File("./", fileName);
                  }

                  private void additionalPerformedOperations(Cursor cursor) {
                    int temp = 1 + 2;

                    int id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    System.out.println(temp);
                    String fileName = cursor.getString(id);

                    temp += 2;
                    File fileObject = new File("./", fileName);
                  }

                  private void callChainStartingAtContext(Context context, Uri returnUri) {
                    ContentResolver contentResolver = context.getContentResolver();
                    Cursor cursor = contentResolver.query(returnUri, null, null, null, null);
                    int id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(id);

                    File fileObject = new File("./", fileName);
                  }

                  private void unsanitizedFilenameUsedTwice(Cursor cursor) {
                    int id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(id);

                    File fileObject = new File("./", fileName);
                    new File("./", fileName);
                  }

                  private void columnNameAsString(Cursor cursor) {
                    int id = cursor.getColumnIndex("_display_name");
                    String fileName = cursor.getString(id);

                    File fileObject = new File("./", fileName);
                  }

                  private void usingProvidedFilenameAsQualifiedExpression(Cursor cursor) {
                    int id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    File fileObject = new File("./", cursor.getString(id));
                  }

                  private void useGetColumnIndexOrThrow(Cursor cursor) {
                    int id = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(id);

                    File fileObject = new File("./", fileName);
                  }
                }
            """)
                .indented(),
            kotlin(
                    """
                import android.database.Cursor
                import android.content.Context
                import android.provider.OpenableColumns
                import java.io.File
                import android.content.ContentResolver
                import android.net.Uri

                class TestClass {
                  private fun cursorFilenameAssignedToVariables(cursor: Cursor) {
                    val fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))

                    val fileObject = File("./", fileName)
                  }

                  private fun additionalPerformedOperations(cursor: Cursor) {
                    var temp = 1 + 2

                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    println(temp)
                    val fileName = cursor.getString(id)

                    temp += 2
                    val fileObject = File("./", fileName)
                  }

                  private fun callChainStartingAtContext(cursor: Cursor) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(id)

                    val fileObject = File("./", fileName)
                  }

                  private fun unsanitizedFilenameUsedTwice(cursor: Cursor) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(id)

                    val fileObject = File("./", fileName)
                    File("./", fileName)
                  }

                  private fun columnNameAsString(cursor: Cursor) {
                    val id = cursor.getColumnIndex("_display_name")
                    val fileName = cursor.getString(id)

                    val fileObject = File("./", fileName)
                  }

                  private fun usingProvidedFilenameAsQualifiedExpression(cursor: Cursor) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                    val fileObject = File("./", cursor.getString(id))
                  }

                  private fun useGetColumnIndexOrThrow(cursor: Cursor) {
                    val id = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(id)

                    val fileObject = File("./", fileName)
                  }
                }
                """)
                .indented(),
        )
        .run()
        .expect(
            """
            src/TestClass.java:12: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                File fileObject = new File("./", fileName);
                                                 ~~~~~~~~
            src/TestClass.java:23: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                File fileObject = new File("./", fileName);
                                                 ~~~~~~~~
            src/TestClass.java:32: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                File fileObject = new File("./", fileName);
                                                 ~~~~~~~~
            src/TestClass.java:39: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                File fileObject = new File("./", fileName);
                                                 ~~~~~~~~
            src/TestClass.java:40: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                new File("./", fileName);
                               ~~~~~~~~
            src/TestClass.java:47: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                File fileObject = new File("./", fileName);
                                                 ~~~~~~~~
            src/TestClass.java:53: Warning: Using cursor.getString(id) is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                File fileObject = new File("./", cursor.getString(id));
                                                 ~~~~~~~~~~~~~~~~~~~~
            src/TestClass.java:60: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                File fileObject = new File("./", fileName);
                                                 ~~~~~~~~
            src/TestClass.kt:12: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                val fileObject = File("./", fileName)
                                            ~~~~~~~~
            src/TestClass.kt:23: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                val fileObject = File("./", fileName)
                                            ~~~~~~~~
            src/TestClass.kt:30: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                val fileObject = File("./", fileName)
                                            ~~~~~~~~
            src/TestClass.kt:37: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                val fileObject = File("./", fileName)
                                            ~~~~~~~~
            src/TestClass.kt:38: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                File("./", fileName)
                           ~~~~~~~~
            src/TestClass.kt:45: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                val fileObject = File("./", fileName)
                                            ~~~~~~~~
            src/TestClass.kt:51: Warning: Using cursor.getString(id) is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                val fileObject = File("./", cursor.getString(id))
                                            ~~~~~~~~~~~~~~~~~~~~
            src/TestClass.kt:58: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                val fileObject = File("./", fileName)
                                            ~~~~~~~~
            0 errors, 16 warnings
            """)
  }

  @Test
  fun testUnusedFilename_shouldNotWarn() {
    lint()
        .files(
            java(
                    """
                import android.database.Cursor;
                import android.provider.OpenableColumns;

                class TestClass {
                  private void filenameNotUsed(Cursor cursor) {
                    int id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(id);
                  }
                }
            """)
                .indented(),
            kotlin(
                    """
                import android.database.Cursor
                import android.provider.OpenableColumns

                class TestClass {
                  private fun filenameNotUsed(cursor: Cursor) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(id)
                  }
                }
                """)
                .indented(),
        )
        .run()
        .expectClean()
  }

  @Test
  fun testFilenamePotentiallySanitized_shouldNotWarn() {
    lint()
        .files(
            java(
                    """
                import android.database.Cursor;
                import android.provider.OpenableColumns;
                import java.io.File;

                class TestClass {
                  private String sanitizeFilename(String displayName) {
                    String[] badCharacters = new String[] { "..", "/" };
                    String[] segments = displayName.split("/");
                    String fileName = segments[segments.length - 1];
                    for (String suspString : badCharacters) {
                        fileName = fileName.replace(suspString, "_");
                    }
                    return fileName;
                  }

                  private Boolean isValid(String fileName) {
                    return true;
                  }

                  private void filenameIsSanitized(Cursor cursor) {
                    int id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(id);

                    String sanitizedFilename = sanitizeFilename(fileName);
                    File fileObject = new File("./", sanitizedFilename);
                  }

                  private void filenameCheckedIfValid(Cursor cursor) {
                    int id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(id);

                    if (isValid(fileName)) {
                      new File("./", fileName);
                    }
                  }

                  private void filenameReceiverOfMethodCall(Cursor cursor) {
                    int id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(id);

                    fileName = fileName.replace('.', '_');
                    File fileObject = new File("./", fileName);
                  }
                }
            """)
                .indented(),
            kotlin(
                    """
                import android.database.Cursor
                import android.provider.OpenableColumns
                import java.io.File

                class TestClass {
                  private fun sanitizeFilename(displayName: String): String {
                    val badCharacters = arrayOf("..", "/")
                    val segments = displayName.split("/")
                    var fileName = segments[segments.size - 1]
                    for (suspString in badCharacters) {
                        fileName = fileName.replace(suspString, "_")
                    }
                    return fileName
                  }

                  private fun isValid(filename: String): Boolean {
                    return true
                  }

                  private fun filenameIsSanitized(cursor: Cursor) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(id)

                    val sanitizedFilename = sanitizeFilename(fileName)
                    val fileObject = File("./", sanitizedFilename)
                  }

                  private fun filenameCheckedIfValid(cursor: Cursor) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(id)

                    if (isValid(fileName)) {
                      File("./", fileName)
                    }
                  }

                  private fun filenameReceiverOfMethodCall(cursor: Cursor) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    var fileName = cursor.getString(id)

                    fileName = fileName.replace('.', '_')
                    val fileObject = File("./", fileName)
                  }
                }
                """)
                .indented(),
        )
        .run()
        .expectClean()
  }

  @Test
  fun testFilenameEscapesToClassField_shouldWarn() {
    lint()
        .files(
            java(
                    """
                import android.database.Cursor;
                import android.provider.OpenableColumns;
                import java.io.File;
                import android.util.Log;

                class TestClass {
                  private void filenameEscapesThroughClassField(Cursor cursor) {
                    int id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(id);

                    class Data {
                      public String fileName = null;
                    }
                    Data data = new Data();
                    data.fileName = fileName;

                    new File("./", data.fileName);
                    new File("./", fileName);
                  }
                }
            """)
                .indented(),
            kotlin(
                    """
                import android.database.Cursor
                import android.provider.OpenableColumns
                import java.io.File
                import android.util.Log

                class TestClass {
                  private fun filenameEscapesThroughClassField(cursor: Cursor) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(id)

                    class Data {
                      var fileName: String? = null
                    }
                    val data = Data()
                    data.fileName = fileName

                    File("./", data.fileName)
                    File("./", fileName)
                  }
                }
                """)
                .indented(),
        )
        .run()
        .expect(
            """
            src/TestClass.java:18: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                new File("./", fileName);
                               ~~~~~~~~
            src/TestClass.kt:18: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                File("./", fileName)
                           ~~~~~~~~
            0 errors, 2 warnings
      """)
  }

  @Test
  fun testFilenameEscapesToIgnoredCases_shouldWarn() {
    lint()
        .files(
            java(
                    """
                import android.database.Cursor;
                import android.provider.OpenableColumns;
                import java.io.File;
                import android.util.Log;

                class TestClass {
                  private void sanitizeFilename(String fileName) {
                    // sanitize the filename
                  }

                  private void filenamePassedToMethodAfterUsage(Cursor cursor) {
                    int id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(id);

                    File fileObject = new File("./", fileName);
                    sanitizeFilename(fileName);
                  }

                  private void filenamePassedToLoggingMethods(Cursor cursor) {
                    int id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    String fileName = cursor.getString(id);

                    Log.d("foo", fileName);

                    File fileObject = new File("./", fileName);
                  }
                }
            """)
                .indented(),
            kotlin(
                    """
                import android.database.Cursor
                import android.provider.OpenableColumns
                import java.io.File
                import android.util.Log

                class TestClass {
                  private fun sanitizeFilename(fileName: String) {
                    // sanitize the filename
                  }

                  private fun filenamePassedToMethodAfterUsage(cursor: Cursor) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(id)

                    val fileObject = File("./", fileName)
                    sanitizeFilename(fileName)
                  }

                  private fun filenamePassedToLoggingMethods(cursor: Cursor) {
                    val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(id)

                    println(fileName)
                    Log.d("foo", fileName)

                    val fileObject = File("./", fileName)
                  }
                }
                """)
                .indented(),
        )
        .run()
        .expect(
            """
            src/TestClass.java:15: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                File fileObject = new File("./", fileName);
                                                 ~~~~~~~~
            src/TestClass.java:25: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                File fileObject = new File("./", fileName);
                                                 ~~~~~~~~
            src/TestClass.kt:15: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                val fileObject = File("./", fileName)
                                            ~~~~~~~~
            src/TestClass.kt:26: Warning: Using fileName is unsafe as it is a filename obtained directly from a ContentProvider. You should sanitize it before using it for creating a File. [UnsanitizedContentProviderFilename]
                val fileObject = File("./", fileName)
                                            ~~~~~~~~
            0 errors, 4 warnings
      """)
  }

  @Test
  fun testDifferentColumnNames_shouldNotWarn() {
    lint()
        .files(
            java(
                    """
                import android.database.Cursor;
                import android.provider.OpenableColumns;
                import java.io.File;

                class TestClass {
                  private void retrievedSizeColumn(Cursor cursor) {
                    String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
                    File fileObject = new File("./", fileName);
                  }

                  private void useDifferentColumnNameLiteral(Cursor cursor) {
                    int id = cursor.getColumnIndex("_column_name");
                    String fileName = cursor.getString(id);
                    File fileObject = new File("./", fileName);
                  }
                }
            """)
                .indented(),
            kotlin(
                    """
                import android.database.Cursor
                import android.provider.OpenableColumns
                import java.io.File

                class TestClass {
                  private fun retrievedSizeColumn(cursor: Cursor) {
                    val fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE))
                    val fileObject = File("./", fileName)
                  }

                  private fun useDifferentColumnNameLiteral(cursor: Cursor) {
                    val id = cursor.getColumnIndex("_column_name")
                    val fileName = cursor.getString(id)
                    val fileObject = File("./", fileName)
                  }
                }
                """)
                .indented(),
        )
        .run()
        .expectClean()
  }

  @Test
  fun testUseReceivedObjects_shouldNotWarn() {
    lint()
        .files(
            java(
                    """
                import android.database.Cursor;
                import java.io.File;

                class TestClass {
                  private void useReceivedColumnId(Cursor cursor, int id) {
                    String fileName = cursor.getString(id);
                    File fileObject = new File("./", fileName);
                  }

                  private void useReceivedFilename(String fileName) {
                    File fileObject = new File("./", fileName);
                  }
                }
            """)
                .indented(),
            kotlin(
                    """
                import android.database.Cursor
                import java.io.File

                class TestClass {
                  private fun useReceivedColumnId(cursor: Cursor, id: Int) {
                    val fileName = cursor.getString(id)
                    val fileObject = File("./", fileName)
                  }

                  private fun useReceivedFilename(fileName: String) {
                    val fileObject = File("./", fileName)
                  }
                }
                """)
                .indented(),
        )
        .run()
        .expectClean()
  }

  @Test
  fun testSuppressWarning_shouldNotWarn() {
    lint()
        .files(
            java(
                    """
                import android.database.Cursor;
                import android.provider.OpenableColumns;
                import java.io.File;

                class TestClass {
                  @android.annotation.SuppressLint("UnsanitizedContentProviderFilename")
                  private void suppressWarningAtMethodLevel(Cursor cursor) {
                    String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    File fileObject = new File("./", fileName);
                  }

                  private void suppressWarningAtSpecificLocationLevel(Cursor cursor) {
                    String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                    @android.annotation.SuppressLint("UnsanitizedContentProviderFilename")
                    File fileObject = new File("./", fileName);
                  }
                }
            """)
                .indented(),
            kotlin(
                    """
                import android.database.Cursor
                import android.provider.OpenableColumns
                import java.io.File

                class TestClass {
                  @android.annotation.SuppressLint("UnsanitizedContentProviderFilename")
                  private fun suppressWarningAtMethodLevel(cursor: Cursor) {
                    val fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    val fileObject = File("./", fileName)
                  }

                  private fun suppressWarningAtSpecificLocationLevel(cursor: Cursor) {
                    val fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))

                    @android.annotation.SuppressLint("UnsanitizedContentProviderFilename")
                    val fileObject = File("./", fileName)
                  }
                }
                """)
                .indented(),
        )
        .run()
        .expectClean()
  }
}
