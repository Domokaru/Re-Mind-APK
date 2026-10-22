package com.hallauer.remind.data.provider

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.hallauer.remind.core.util.PhoneNumberNormalizer

data class PickedContactInfo(
    val contactId: Long,
    val lookupKey: String,
    val displayName: String,
    val photoUri: String?,
    val phoneNumbers: List<ContactPhoneNumber>
)

data class ContactPhoneNumber(
    val rawNumber: String,
    val normalizedNumber: String,
    val typeLabel: String,
    val isPrimary: Boolean
)

class ContactsProviderHelper(private val context: Context) {

    /**
     * Queries all contacts with phone numbers from ContactsContract.
     */
    fun searchContacts(query: String = ""): List<PickedContactInfo> {
        val result = mutableListOf<PickedContactInfo>()
        val contentResolver = context.contentResolver

        val selection = if (query.isNotBlank()) {
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ? AND ${ContactsContract.Contacts.HAS_PHONE_NUMBER} = 1"
        } else {
            "${ContactsContract.Contacts.HAS_PHONE_NUMBER} = 1"
        }
        val selectionArgs = if (query.isNotBlank()) arrayOf("%$query%") else null

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_URI
        )

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
        )

        cursor?.use { c ->
            val idIdx = c.getColumnIndex(ContactsContract.Contacts._ID)
            val lookupIdx = c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
            val nameIdx = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val photoIdx = c.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)

            while (c.moveToNext()) {
                val id = c.getLong(idIdx)
                val lookup = c.getString(lookupIdx) ?: ""
                val name = c.getString(nameIdx) ?: "Unknown"
                val photo = c.getString(photoIdx)

                val numbers = queryPhoneNumbersForContact(id)
                if (numbers.isNotEmpty()) {
                    result.add(
                        PickedContactInfo(
                            contactId = id,
                            lookupKey = lookup,
                            displayName = name,
                            photoUri = photo,
                            phoneNumbers = numbers
                        )
                    )
                }
            }
        }
        return result
    }

    /**
     * Retrieves all phone numbers for a contact ID, sorting TYPE_MOBILE first.
     */
    fun queryPhoneNumbersForContact(contactId: Long): List<ContactPhoneNumber> {
        val list = mutableListOf<ContactPhoneNumber>()
        val phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
        )

        val cursor = context.contentResolver.query(
            phoneUri,
            projection,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )

        cursor?.use { c ->
            val numIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
            val labelIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)
            val primaryIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)

            while (c.moveToNext()) {
                val rawNumber = c.getString(numIdx) ?: continue
                val type = c.getInt(typeIdx)
                val label = c.getString(labelIdx)
                val isPrimary = c.getInt(primaryIdx) == 1

                val typeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                    context.resources,
                    type,
                    label ?: "Other"
                ).toString()

                list.add(
                    ContactPhoneNumber(
                        rawNumber = rawNumber,
                        normalizedNumber = PhoneNumberNormalizer.normalize(rawNumber),
                        typeLabel = typeLabel,
                        isPrimary = isPrimary || type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    )
                )
            }
        }

        // Sort so mobile/primary comes first
        return list.sortedWith(
            compareByDescending<ContactPhoneNumber> { it.isPrimary }
                .thenBy { it.typeLabel.lowercase() != "mobile" }
        )
    }

    /**
     * Verifies if a stored lookupKey still resolves to a contact with the stored normalized number.
     */
    fun verifyContactNumber(lookupKey: String, expectedNormalizedNumber: String): VerificationResult {
        val lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)
        val cursor = context.contentResolver.query(
            lookupUri,
            arrayOf(ContactsContract.Contacts._ID),
            null,
            null,
            null
        )

        var foundContactId: Long? = null
        cursor?.use { c ->
            if (c.moveToFirst()) {
                foundContactId = c.getLong(0)
            }
        }

        if (foundContactId == null) {
            return VerificationResult.ContactNotFound
        }

        val currentNumbers = queryPhoneNumbersForContact(foundContactId!!)
        val hasMatch = currentNumbers.any { PhoneNumberNormalizer.areEqual(it.normalizedNumber, expectedNormalizedNumber) }

        return if (hasMatch) {
            VerificationResult.Valid
        } else {
            VerificationResult.NumberChangedOrRemoved(currentNumbers)
        }
    }

    sealed class VerificationResult {
        data object Valid : VerificationResult()
        data object ContactNotFound : VerificationResult()
        data class NumberChangedOrRemoved(val availableNumbers: List<ContactPhoneNumber>) : VerificationResult()
    }
}
