/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.providers.contacts;

import com.google.android.collect.Lists;

import android.accounts.Account;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.Settings;
import android.test.mock.MockContentProvider;
import android.test.suitebuilder.annotation.LargeTest;


/**
 * Unit tests for {@link ContactsProvider2}, directory functionality.
 *
 * Run the test like this:
 * <code>
 * adb shell am instrument -e class com.android.providers.contacts.DirectoryTest -w \
 *         com.android.providers.contacts.tests/android.test.InstrumentationTestRunner
 * </code>
 */
@LargeTest
public class DirectoryTest extends BaseContactsProvider2Test {

    public void testDefaultDirectory() {
        ContentValues values = new ContentValues();
        Uri defaultDirectoryUri =
            ContentUris.withAppendedId(Directory.CONTENT_URI, Directory.DEFAULT);

        values.put(Directory.PACKAGE_NAME, "contactsTestPackage");
        values.put(Directory.DIRECTORY_AUTHORITY, ContactsContract.AUTHORITY);
        values.put(Directory.TYPE_RESOURCE_ID, R.string.default_directory);
        values.put(Directory.EXPORT_SUPPORT, Directory.EXPORT_SUPPORT_NONE);
        values.putNull(Directory.ACCOUNT_NAME);
        values.putNull(Directory.ACCOUNT_TYPE);
        values.putNull(Directory.DISPLAY_NAME);

        assertStoredValues(defaultDirectoryUri, values);
    }

    public void testInvisibleLocalDirectory() {
        ContentValues values = new ContentValues();
        Uri defaultDirectoryUri =
            ContentUris.withAppendedId(Directory.CONTENT_URI, Directory.LOCAL_INVISIBLE);

        values.put(Directory.PACKAGE_NAME, "contactsTestPackage");
        values.put(Directory.DIRECTORY_AUTHORITY, ContactsContract.AUTHORITY);
        values.put(Directory.TYPE_RESOURCE_ID, R.string.local_invisible_directory);
        values.put(Directory.EXPORT_SUPPORT, Directory.EXPORT_SUPPORT_NONE);
        values.putNull(Directory.ACCOUNT_NAME);
        values.putNull(Directory.ACCOUNT_TYPE);
        values.putNull(Directory.DISPLAY_NAME);

        assertStoredValues(defaultDirectoryUri, values);
    }

    public void testDirectoryInsert() {
        ContentValues values = new ContentValues();
        values.put(Directory.PACKAGE_NAME, ContactsActor.PACKAGE_GREY);
        values.put(Directory.DIRECTORY_AUTHORITY, "my_authority");
        values.put(Directory.TYPE_RESOURCE_ID, 42);
        values.put(Directory.DISPLAY_NAME, "Universe");
        values.put(Directory.EXPORT_SUPPORT, Directory.EXPORT_SUPPORT_ANY_ACCOUNT);
        values.put(Directory.ACCOUNT_NAME, "accountName");
        values.put(Directory.ACCOUNT_TYPE, "accountType");

        mActor.ensureCallingPackage();

        Uri uri = mResolver.insert(Directory.CONTENT_URI, values);
        assertStoredValues(uri, values);
    }

    public void testDirectoryInsertWrongPackage() {
        ContentValues values = new ContentValues();
        values.put(Directory.PACKAGE_NAME, "wrong.package");
        values.put(Directory.DIRECTORY_AUTHORITY, "my_authority");

        mActor.ensureCallingPackage();

        try {
            mResolver.insert(Directory.CONTENT_URI, values);
            fail("Was expecting an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // Expected
        }
    }

    public void testDirectoryUpdate() {
        ContentValues values = new ContentValues();
        values.put(Directory.PACKAGE_NAME, ContactsActor.PACKAGE_GREY);
        values.put(Directory.DIRECTORY_AUTHORITY, "my_authority");
        values.put(Directory.TYPE_RESOURCE_ID, 42);
        values.put(Directory.DISPLAY_NAME, "Universe");
        values.put(Directory.EXPORT_SUPPORT, Directory.EXPORT_SUPPORT_ANY_ACCOUNT);
        values.put(Directory.ACCOUNT_NAME, "accountName");
        values.put(Directory.ACCOUNT_TYPE, "accountType");

        mActor.ensureCallingPackage();

        Uri uri = mResolver.insert(Directory.CONTENT_URI, values);

        values.put(Directory.DIRECTORY_AUTHORITY, "different_authority");
        values.put(Directory.TYPE_RESOURCE_ID, 43);
        values.put(Directory.DISPLAY_NAME, "Beyond Universe");
        values.put(Directory.EXPORT_SUPPORT, Directory.EXPORT_SUPPORT_NONE);
        values.put(Directory.ACCOUNT_NAME, "newName");
        values.put(Directory.ACCOUNT_TYPE, "newType");

        int count = mResolver.update(uri, values, null, null);
        assertEquals(1, count);
        assertStoredValues(uri, values);
    }

    public void testDirectoryUpdateWrongPackage() {
        ContentValues values = new ContentValues();
        values.put(Directory.PACKAGE_NAME, ContactsActor.PACKAGE_GREY);
        values.put(Directory.DIRECTORY_AUTHORITY, "my_authority");

        mActor.ensureCallingPackage();
        Uri uri = mResolver.insert(Directory.CONTENT_URI, values);

        values.put(Directory.DIRECTORY_AUTHORITY, "different_authority");

        mActor.packageName = "different.package";
        mActor.ensureCallingPackage();

        try {
            mResolver.update(uri, values, null, null);
            fail("Was expecting an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // Expected
        }
    }

    public void testDirectorDelete() {
        ContentValues values = new ContentValues();
        values.put(Directory.PACKAGE_NAME, ContactsActor.PACKAGE_GREY);
        values.put(Directory.DIRECTORY_AUTHORITY, "my_authority");

        mActor.ensureCallingPackage();

        Uri uri = mResolver.insert(Directory.CONTENT_URI, values);

        mResolver.delete(uri, null, null);

        assertEquals(0, getCount(uri, null, null));
    }

    public void testDirectorDeleteWrongPackage() {
        ContentValues values = new ContentValues();
        values.put(Directory.PACKAGE_NAME, ContactsActor.PACKAGE_GREY);
        values.put(Directory.DIRECTORY_AUTHORITY, "my_authority");

        mActor.ensureCallingPackage();
        Uri uri = mResolver.insert(Directory.CONTENT_URI, values);

        values.put(Directory.DIRECTORY_AUTHORITY, "different_authority");

        mActor.packageName = "different.package";
        mActor.ensureCallingPackage();
        try {
            mResolver.delete(uri, null, null);
            fail("Was expecting an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // Expected
        }
    }

    public void testAccountRemoval() {
        ((ContactsProvider2)getProvider()).onAccountsUpdated(
                new Account[]{new Account("accountName", "accountType")});

        ContentValues values = new ContentValues();
        values.put(Directory.PACKAGE_NAME, ContactsActor.PACKAGE_GREY);
        values.put(Directory.DIRECTORY_AUTHORITY, "my_authority");
        values.put(Directory.ACCOUNT_NAME, "accountName");
        values.put(Directory.ACCOUNT_TYPE, "accountType");

        mActor.ensureCallingPackage();
        Uri uri = mResolver.insert(Directory.CONTENT_URI, values);

        ((ContactsProvider2)getProvider()).onAccountsUpdated(
                new Account[]{new Account("name", "type")});

        // Removing the account should trigger the removal of the directory
        assertEquals(0, getCount(uri, null, null));
    }

    public void testPackageRemoval() {
        ContentValues values = new ContentValues();
        values.put(Directory.PACKAGE_NAME, ContactsActor.PACKAGE_GREY);
        values.put(Directory.DIRECTORY_AUTHORITY, "my_authority");

        mActor.ensureCallingPackage();
        Uri uri = mResolver.insert(Directory.CONTENT_URI, values);

        ((ContactsProvider2)getProvider()).onPackageUninstalled(ContactsActor.PACKAGE_GREY);

        // Uninstalling the package should trigger the removal of the directory
        assertEquals(0, getCount(uri, null, null));
    }

    public void testForwardingToLocalContacts() {
        long contactId = queryContactId(createRawContactWithName());

        Uri contentUri = Contacts.CONTENT_URI.buildUpon().appendQueryParameter(
                ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Directory.DEFAULT)).build();

        Cursor cursor = mResolver.query(contentUri,
                new String[]{Contacts._ID, Contacts.DISPLAY_NAME}, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(contactId, cursor.getLong(0));
        assertEquals("John Doe", cursor.getString(1));
        cursor.close();
    }

    public void testForwardingToLocalInvisibleContacts() {
        long visibleContactId = queryContactId(createRawContactWithName("Bob", "Parr"));

        assertStoredValue(ContentUris.withAppendedId(Contacts.CONTENT_URI, visibleContactId),
                Contacts.IN_VISIBLE_GROUP, "1");

        long hiddenContactId = queryContactId(createRawContactWithName("Helen", "Parr",
                new Account("accountName", "accountType")));

        assertStoredValue(ContentUris.withAppendedId(Contacts.CONTENT_URI, hiddenContactId),
                Contacts.IN_VISIBLE_GROUP, "0");

        Uri contentUri = Contacts.CONTENT_URI.buildUpon().appendQueryParameter(
                ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(Directory.LOCAL_INVISIBLE))
                .build();

        Cursor cursor = mResolver.query(contentUri,
                new String[]{Contacts._ID, Contacts.DISPLAY_NAME}, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(hiddenContactId, cursor.getLong(0));
        assertEquals("Helen Parr", cursor.getString(1));
        cursor.close();

        Uri filterUri = Contacts.CONTENT_FILTER_URI.buildUpon().appendEncodedPath("parr")
                .appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY,
                        String.valueOf(Directory.LOCAL_INVISIBLE)).build();

        cursor = mResolver.query(filterUri,
                new String[]{Contacts._ID, Contacts.DISPLAY_NAME}, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(hiddenContactId, cursor.getLong(0));
        assertEquals("Helen Parr", cursor.getString(1));
        cursor.close();
    }

    public void testForwardingToDirectoryProvider() throws Exception {
        addProvider(TestProvider.class, "test_authority");

        ContentValues values = new ContentValues();
        values.put(Directory.PACKAGE_NAME, ContactsActor.PACKAGE_GREY);
        values.put(Directory.DIRECTORY_AUTHORITY, "test_authority");

        mActor.ensureCallingPackage();
        Uri uri = mResolver.insert(Directory.CONTENT_URI, values);
        long directoryId = ContentUris.parseId(uri);

        Uri contentUri = Contacts.CONTENT_URI.buildUpon().appendQueryParameter(
                ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(directoryId)).build();

        // The request should be forwarded to TestProvider, which will simply
        // package arguments and return them to us for verification
        Cursor cursor = mResolver.query(contentUri,
                new String[]{"f1", "f2"}, "query", new String[]{"s1", "s2"}, "so");
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals("[f1, f2]", cursor.getString(cursor.getColumnIndex("projection")));
        assertEquals("query", cursor.getString(cursor.getColumnIndex("selection")));
        assertEquals("[s1, s2]", cursor.getString(cursor.getColumnIndex("selectionArgs")));
        assertEquals("so", cursor.getString(cursor.getColumnIndex("sortOrder")));
        cursor.close();
    }

    public static class TestProvider extends MockContentProvider {

        @Override
        public void attachInfo(Context context, ProviderInfo info) {
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                String sortOrder) {
            MatrixCursor cursor = new MatrixCursor(new String[] {
                    "projection", "selection", "selectionArgs", "sortOrder"
            });
            cursor.addRow(new Object[] {
                Lists.newArrayList(projection).toString(),
                selection,
                Lists.newArrayList(selectionArgs).toString(),
                sortOrder
            });
            return cursor;
        }
    }
}

