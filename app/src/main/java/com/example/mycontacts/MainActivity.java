package com.example.mycontacts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
{

    ListView contactListView;
    ArrayList<ContactItem> contactItems;
    ListViewAdapter listViewAdapter;
    final private int CONTACTS_REQUEST_CODE = 123;

    HashMap<String , ContactItem> contactItemsHashMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactListView = findViewById(R.id.CONTACT_LIST_VIEW);

        contactItemsHashMap = new HashMap<>();
        contactItems = new ArrayList<>();

        listViewAdapter = new ListViewAdapter(contactItems);

        contactListView.setAdapter(listViewAdapter);

        CheckUserPermission();



    }


    private void CheckUserPermission()
    {
        if(Build.VERSION.SDK_INT >= 23)
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                            != PackageManager.PERMISSION_GRANTED&&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS
                                ,Manifest.permission.WRITE_CONTACTS,Manifest.permission.CALL_PHONE}, CONTACTS_REQUEST_CODE);

                return;
            }
        }

        ReadContacts();
        listViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == CONTACTS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1]==PackageManager.PERMISSION_GRANTED
                    && grantResults[2]==PackageManager.PERMISSION_GRANTED) {
                ReadContacts();
                listViewAdapter.notifyDataSetChanged();

            } else {
                Toast.makeText(getApplicationContext(), "You can not use these feature without location access",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void ReadContacts()
    {
        contactItems.clear();
        contactItemsHashMap.clear();

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                ,null , null,null , ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

        assert cursor != null;
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));


                ContactItem contactItem = new ContactItem(name,number);

                contactItemsHashMap.put(number,contactItem);

            } while (cursor.moveToNext());
        }

        TreeMap<String , ContactItem> SortedContacts = new TreeMap<>(contactItemsHashMap);

        for (Map.Entry mapElement : SortedContacts.entrySet())
        {

            ContactItem contact = (ContactItem) mapElement.getValue();
            contactItems.add(contact);
        }

       cursor.close();

    }


    private void ReadSpecificContacts(String Name)
    {
        contactItems.clear();
        contactItemsHashMap.clear();

        String Names[] = {"%"+Name+"%"};

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                ,null , ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +" like ? ",Names , ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

        assert cursor != null;
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));


                ContactItem contactItem = new ContactItem(name,number);

                contactItemsHashMap.put(number,contactItem);

            } while (cursor.moveToNext());
        }
        TreeMap<String , ContactItem> SortedContacts = new TreeMap<>(contactItemsHashMap);

        for (Map.Entry mapElement : SortedContacts.entrySet())
        {
            ContactItem contact = (ContactItem) mapElement.getValue();
            contactItems.add(contact);
        }

        cursor.close();
    }


    class ListViewAdapter extends BaseAdapter
    {
        ArrayList<ContactItem> contactItemArrayList;

        public ListViewAdapter(ArrayList<ContactItem> contactItemArrayList) {
            this.contactItemArrayList = contactItemArrayList;
        }

        @Override
        public int getCount() {
            return contactItemArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return contactItemArrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            LayoutInflater layoutInflater = getLayoutInflater();
            final View contactsView =  layoutInflater.inflate(R.layout.contact_list_item,null);

            final ContactItem contactItem = contactItemArrayList.get(i);

            TextView contactNameTextView = contactsView.findViewById(R.id.CONTACT_NAME_TEXT_VIEW);
            Button callButton = contactsView.findViewById(R.id.CALL_BUTTON);
            Button deleteButton = contactsView.findViewById(R.id.DELETE_BUTTON);

            contactNameTextView.setText(contactItem.getName());

            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + contactItem.getPhoneNumber() + ""));
                    try {
                        startActivity(intent);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(view.getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DeleteContact(contactItem);
                }
            });

            return contactsView;
        }


    }

    private void DeleteContact(final ContactItem contactItem)
    {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        alertBuilder.setMessage("Are you sure you want to add a new one?")
                .setTitle("Configuration")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                ,null , null,null , ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                        assert cursor != null;
                        if (cursor.moveToFirst()) {
                            do {
                                try {
                                    String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                                    String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                    if (number.equals(contactItem.getPhoneNumber()))
                                    {
                                        cursor.close();

                                        String[] Numbers = {contactItem.getPhoneNumber()};

                                        getContentResolver().delete(uri, ContactsContract.CommonDataKinds.Phone.NUMBER +"=?", Numbers);

                                        ReadContacts();
                                        listViewAdapter.notifyDataSetChanged();

                                        break;
                                    }
                                }
                                catch (Exception e)
                                {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            } while (cursor.moveToNext());
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                })
                .show();
    }

    private void insertContact()
    {
        Intent contactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
        contactIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

        startActivityForResult(contactIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == 1)
        {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Added Contact", Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled Added Contact", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s)
            {
                ReadSpecificContacts(s);
                listViewAdapter.notifyDataSetChanged();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {
                ReadSpecificContacts(s);
                listViewAdapter.notifyDataSetChanged();
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                ReadContacts();
                listViewAdapter.notifyDataSetChanged();
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()){
            case R.id.ADD_NEW_CONTACT:
                insertContact();
                ReadContacts();
                listViewAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}

