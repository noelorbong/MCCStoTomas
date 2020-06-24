package com.example.mccstotomas.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import com.example.mccstotomas.Model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class SqlLiteHelper extends AppCompatActivity {

    public static int dbversion = 2;
    public static String dbname = "DBName";
    public static String dbTable = "DBTable";

    public static String cul1_userId = "UserId";
    public static String cul2_name = "Name";
    public static String cul3_address = "Address";
    public static String cul4_dob = "Dob";
    public static String cul5_photo = "Photo";
    public static String cul6_email = "Email";
    public static String cul7_password = "Password";
    public static String cul8_number = "Number";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, dbname, null, dbversion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + dbTable + " (_id INTEGER PRIMARY KEY autoincrement,"
                    + cul1_userId +" INTEGER"
                    + ", " + cul2_name
                    +", "+cul3_address
                    +", "+cul4_dob
                    +", "+cul5_photo
                    +", "+cul6_email
                    +", "+cul7_password
                    +", "+cul8_number
                    + " )");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + dbTable);
            onCreate(db);
        }
    }

    //establsh connection with SQLiteDataBase
    private final Context c;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase sqlDb;

    public SqlLiteHelper(Context context) {
        this.c = context;
    }

    public SqlLiteHelper open() throws SQLException {
        dbHelper = new DatabaseHelper(c);
        sqlDb = dbHelper.getWritableDatabase();
        return this;
    }

    public void insertUser(UserModel mpUser){
        _insertUser(mpUser);
    }

    public List<UserModel> getAllUser(){
        return _getAllUser();
    }

    public UserModel getSpecificUser(){
        return _getSpecificUser();
    }
    public void deleteUser(){
        _deleteUser();
    }

    private void _insertUser(UserModel userM) {
        sqlDb.execSQL("INSERT INTO "+dbTable+" ("
                +cul1_userId
                +", "+cul2_name
                +", "+cul3_address
                +", "+cul4_dob
                +", "+cul5_photo
                +", "+cul6_email
                +", "+cul7_password
                +", "+cul8_number
                +") VALUES("
                + "'"+ userM.getId() + "'"
                + ",'" + userM.getName() +"'"
                + ",'" + userM.getAddress() +"'"
                + ",'" + userM.getDob() +"'"
                + ",'" + userM.getPhoto() +"'"
                + ",'" + userM.getEmail() +"'"
                + ",'" + userM.getPassword() +"'"
                + ",'" + userM.getMobileNumber() +"'"
                + ")");
    }
    private List<UserModel> _getAllUser() {
        List<UserModel> userModelList = new ArrayList<>();
        UserModel userM = new UserModel();

        String query = "SELECT _id, "
                +cul1_userId
                +", "+cul2_name
                +", "+cul3_address
                +", "+cul4_dob
                +", "+cul5_photo
                +", "+cul6_email
                +", "+cul7_password
                +", "+cul8_number
                +" FROM "+dbTable +" LIMIT 1";
        Cursor c = sqlDb.rawQuery(query,null);
        int i = 0;
        if (c != null ) {
            if  (c.moveToFirst()) {
                do {
                    userM = new UserModel();
                    userM.setId(c.getInt(c.getColumnIndex(cul1_userId)));
                    userM.setName(c.getString(c.getColumnIndex(cul2_name)));
                    userM.setAddress(c.getString(c.getColumnIndex(cul3_address)));
                    userM.setDob(c.getString(c.getColumnIndex(cul4_dob)));
                    userM.setPhoto(c.getString(c.getColumnIndex(cul5_photo)));
                    userM.setEmail(c.getString(c.getColumnIndex(cul6_email)));
                    userM.setPassword(c.getString(c.getColumnIndex(cul7_password)));
                    userM.setMobileNumber(c.getString(c.getColumnIndex(cul8_number)));
                    userModelList.add(userM);
                    i++;
                }while (c.moveToNext());
            }
        }
        c.close();
        return userModelList;
    }
    private UserModel _getSpecificUser() {
        UserModel userM = new UserModel();

        String query = "SELECT _id, "
                +cul1_userId
                +", "+cul2_name
                +", "+cul3_address
                +", "+cul4_dob
                +", "+cul5_photo
                +", "+cul6_email
                +", "+cul7_password
                +", "+cul8_number
                +" FROM "+dbTable +" LIMIT 1";
        Cursor c = sqlDb.rawQuery(query,null);
        int i = 0;
        if (c != null ) {
            if  (c.moveToFirst()) {
                do {
//                    userM = new UserModel();
                    userM.setId(c.getInt(c.getColumnIndex(cul1_userId)));
                    userM.setName(c.getString(c.getColumnIndex(cul2_name)));
                    userM.setAddress(c.getString(c.getColumnIndex(cul3_address)));
                    userM.setDob(c.getString(c.getColumnIndex(cul4_dob)));
                    userM.setPhoto(c.getString(c.getColumnIndex(cul5_photo)));
                    userM.setEmail(c.getString(c.getColumnIndex(cul6_email)));
                    userM.setPassword(c.getString(c.getColumnIndex(cul7_password)));
                    userM.setMobileNumber(c.getString(c.getColumnIndex(cul8_number)));
                    i++;
                }while (c.moveToNext());
            }
        }
        c.close();
        return userM;
    }
    private void _deleteUser() {
        sqlDb.execSQL("DELETE FROM "+dbTable);
    }
}
