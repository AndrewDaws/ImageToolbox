/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

@file:Suppress("unused")

package com.t8rin.imagetoolbox.core.ui.utils.content_pickers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.utils.provider.rememberLocalEssentials
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.logger.makeLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class ContactPickerImpl(
    val context: Context,
    val pickContact: ManagedActivityResultLauncher<Void?, Uri?>,
    val requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    val onFailure: (Throwable) -> Unit
) : ContactPicker {

    override fun pickContact() {
        "Pick Contact Start".makeLog()
        runCatching {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pickContact.launch(null)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }.onFailure {
            it.makeLog("Pick Contact Failure")
            onFailure(it)
        }.onSuccess {
            "Pick Contact Success".makeLog()
        }
    }

}


@Stable
@Immutable
interface ContactPicker {
    fun pickContact()
}

data class Contact(
    val addresses: List<Address>,
    val emails: List<Email>,
    val name: PersonName,
    val organization: String,
    val phones: List<Phone>,
    val title: String,
    val urls: List<String>
) {
    constructor() : this(
        addresses = emptyList(),
        emails = emptyList(),
        name = PersonName(),
        organization = "",
        phones = emptyList(),
        title = "",
        urls = emptyList()
    )

    data class Address(
        val addressLines: List<String>,
        val type: Int
    )

    data class PersonName(
        val first: String,
        val formattedName: String,
        val last: String,
        val middle: String,
        val prefix: String,
        val pronunciation: String,
        val suffix: String
    ) {
        constructor() : this(
            first = "",
            formattedName = "",
            last = "",
            middle = "",
            prefix = "",
            pronunciation = "",
            suffix = ""
        )

        fun isEmpty() = first.isBlank() &&
                formattedName.isBlank() &&
                last.isBlank() &&
                middle.isBlank() &&
                prefix.isBlank() &&
                pronunciation.isBlank() &&
                suffix.isBlank()
    }

    data class Email(
        val address: String,
        val body: String,
        val subject: String,
        val type: Int
    ) {
        constructor() : this(
            address = "",
            body = "",
            subject = "",
            type = 0
        )

        fun isEmpty(): Boolean = address.isBlank() && body.isBlank() && subject.isBlank()
    }

    data class Phone(
        val number: String,
        val type: Int
    ) {
        constructor() : this(
            number = "",
            type = 0
        )

        fun isEmpty(): Boolean = number.isBlank()
    }

    fun isEmpty(): Boolean =
        addresses.isEmpty() && emails.isEmpty() && name.isEmpty() && organization.isBlank() && phones.isEmpty() && title.isBlank() && urls.isEmpty()
}

@Composable
fun rememberContactPicker(
    onFailure: () -> Unit = {},
    onSuccess: (Contact) -> Unit,
): ContactPicker {
    val essentials = rememberLocalEssentials()
    val pickContact = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { uri ->
            uri?.takeIf {
                it != Uri.EMPTY
            }?.let {
                essentials.coroutineScope.launch {
                    onSuccess(it.parseContact())
                }
            } ?: onFailure()
        }
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pickContact.launch()
        } else {
            essentials.showToast(
                messageSelector = { getString(R.string.grant_contact_permission) },
                icon = Icons.Outlined.PersonOutline
            )
        }
    }

    return remember(pickContact) {
        derivedStateOf {
            ContactPickerImpl(
                context = essentials.context,
                pickContact = pickContact,
                requestPermissionLauncher = requestPermissionLauncher,
                onFailure = {
                    onFailure()
                    essentials.showFailureToast(it)
                }
            )
        }
    }.value
}

private suspend fun Uri.parseContact(): Contact = withContext(Dispatchers.IO) {
    val context = appContext
    val resolver = context.contentResolver
    var contactId: String? = null
    var name = Contact.PersonName()
    var organization = ""
    var title = ""
    val phones = mutableListOf<Contact.Phone>()
    val emails = mutableListOf<Contact.Email>()
    val addresses = mutableListOf<Contact.Address>()
    val urls = mutableListOf<String>()

    resolver.query(this@parseContact, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            contactId = cursor.getStringOrEmpty(ContactsContract.Contacts._ID)
        }
    }

    contactId?.let { id ->
        // StructuredName
        resolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
            arrayOf(id, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE),
            null
        )?.use { c ->
            if (c.moveToFirst()) {
                name = Contact.PersonName(
                    first = c.getStringOrEmpty(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME),
                    formattedName = c.getStringOrEmpty(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME),
                    last = c.getStringOrEmpty(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME),
                    middle = c.getStringOrEmpty(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME),
                    prefix = c.getStringOrEmpty(ContactsContract.CommonDataKinds.StructuredName.PREFIX),
                    pronunciation = c.getStringOrEmpty(ContactsContract.Contacts.PHONETIC_NAME),
                    suffix = c.getStringOrEmpty(ContactsContract.CommonDataKinds.StructuredName.SUFFIX)
                )
            }
        }

        // Phones
        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(id),
            null
        )?.use { c ->
            while (c.moveToNext()) {
                phones.add(
                    Contact.Phone(
                        number = c.getStringOrEmpty(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        type = c.getIntOrZero(ContactsContract.CommonDataKinds.Phone.TYPE)
                    )
                )
            }
        }

        // Emails
        resolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(id),
            null
        )?.use { c ->
            while (c.moveToNext()) {
                emails.add(
                    Contact.Email(
                        address = c.getStringOrEmpty(ContactsContract.CommonDataKinds.Email.ADDRESS),
                        body = "",
                        subject = "",
                        type = c.getIntOrZero(ContactsContract.CommonDataKinds.Email.TYPE)
                    )
                )
            }
        }

        // Addresses
        resolver.query(
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID} = ?",
            arrayOf(id),
            null
        )?.use { c ->
            while (c.moveToNext()) {
                val address = c.getStringOrEmpty(
                    ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
                )
                if (address.isNotBlank()) {
                    addresses.add(
                        Contact.Address(
                            addressLines = address.split("\n"),
                            type = c.getIntOrZero(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)
                        )
                    )
                }
            }
        }

        // Organization + Title
        resolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
            arrayOf(id, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
            null
        )?.use { c ->
            if (c.moveToFirst()) {
                organization =
                    c.getStringOrEmpty(ContactsContract.CommonDataKinds.Organization.COMPANY)
                title = c.getStringOrEmpty(ContactsContract.CommonDataKinds.Organization.TITLE)
            }
        }

        // Websites
        resolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
            arrayOf(id, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE),
            null
        )?.use { c ->
            while (c.moveToNext()) {
                val url = c.getStringOrEmpty(ContactsContract.CommonDataKinds.Website.URL)
                if (url.isNotBlank()) urls.add(url)
            }
        }
    }

    Contact(
        addresses = addresses,
        emails = emails,
        name = name,
        organization = organization,
        phones = phones,
        title = title,
        urls = urls
    )
}

private fun Cursor.getStringOrEmpty(column: String): String =
    runCatching { getString(getColumnIndexOrThrow(column)) }.getOrNull() ?: ""

private fun Cursor.getIntOrZero(column: String): Int =
    runCatching { getInt(getColumnIndexOrThrow(column)) }.getOrNull() ?: 0