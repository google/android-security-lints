package com.android.example

import android.database.Cursor
import android.provider.OpenableColumns
import java.io.File


class FakeContentProviderFilenameSanitization {

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

