package com.example.myapplication;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseDatabaseHelper {

    private static final String DATABASE_URL = "https://sample-pro-4201c-default-rtdb.firebaseio.com";
    private final DatabaseReference databaseReference;

    public FirebaseDatabaseHelper() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL);
        databaseReference = firebaseDatabase.getReference("users");
    }

    public void saveUser(String name, String department, OnCompleteListener<Void> onCompleteListener) {
        String key = databaseReference.push().getKey();
        if (key != null) {
            User user = new User(key, name, department);
            databaseReference.child(key).setValue(user)
                    .addOnCompleteListener(onCompleteListener);
        }
    }

    public void deleteUser(String key, OnCompleteListener<Void> onCompleteListener) {
        if (key != null) {
            databaseReference.child(key).removeValue()
                    .addOnCompleteListener(onCompleteListener);
        }
    }

    public void clearAllUsers(OnCompleteListener<Void> onCompleteListener) {
        databaseReference.removeValue()
                .addOnCompleteListener(onCompleteListener);
    }

    public ValueEventListener attachUsersListener(ValueEventListener valueEventListener) {
        databaseReference.addValueEventListener(valueEventListener);
        return valueEventListener;
    }

    public void detachUsersListener(ValueEventListener valueEventListener) {
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }
}
