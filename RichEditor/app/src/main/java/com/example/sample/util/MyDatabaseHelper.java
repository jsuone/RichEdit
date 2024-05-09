package com.example.sample.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.alarmclock.Group;
import com.example.sample.model.NoteModel;
import com.example.sample.model.TitleModel;
import com.example.sample.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static MyDatabaseHelper instance;
    private SQLiteDatabase database;
    private static final String DATABASE_NAME = "note_db";
    private static final int DATABASE_VERSION = 1;
    private ACache aCache;
    public static final String create_table_user = "create table user(\n" +
            "id INTEGER PRIMARY key autoincrement,\n" +
            "username VARCHAR(255),\n" +
            "ssn INTEGER,\n" +
            "lastUpdateSSN INT,\n" +
            "lastSyncTime VARCHAR(255)\n" +
            ");";

    public static final String create_table_title = "create table title(\n" +
            "guid VARCHAR(255) PRIMARY key ,\n" +
            "title VARCHAR(254) ,\n" +
            "parent_id VARCHAR(255),\n" +
            "level int,\n" +
            "is_open bit(1) not null DEFAULT 0,\n" +
            "is_del bit(1) not null DEFAULT 0,\n" +
            "ssn int ,\n" +
            "create_time VARCHAR(255),\n" +
            "update_time VARCHAR(255),\n" +
            "user_guid VARCHAR(255)\n" +
            ");";
    public static final String create_table_note = "create table note(\n" +
            "guid VARCHAR(255) PRIMARY key ,\n" +
            "theme VARCHAR(255) ,\n" +
            "note_content text,\n" +
            "title_guid VARCHAR(255),\n" +
            "create_time VARCHAR(255),\n" +
            "ssn int ,\n" +
            "update_time VARCHAR(255),\n" +
            "is_del bit(1)  NOT null DEFAULT 0,\n" +
            "user_guid VARCHAR(255),\n" +
            "CONSTRAINT fk_title FOREIGN KEY (title_guid) REFERENCES title(guid) ON DELETE CASCADE\n" +
            ");";
    public static final String create_table_clock = "create table clock(\n" +
            "guid VARCHAR(255),\n" +
            "clock_time VARCHAR(255),\n" +
            "clock_name VARCHAR(255),\n" +
            "is_checked  bit(1) not null DEFAULT 0,\n" +
            "is_repeat  bit(1) not null DEFAULT 0,\n" +
            "ssn int,\n" +
            "is_del  bit(1) not null DEFAULT 0 ,\n" +
            "create_time VARCHAR(255),\n" +
            "update_time VARCHAR(255),\n" +
            "user_guid VARCHAR(255)\n" +
            ");";
//    public static final String create_table_note_title = "CREATE TABLE note_title (\n" +
//            "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
//            "    title_guid VARCHAR(255),\n" +
//            "    note_guid VARCHAR(255),\n" +
//            "    CONSTRAINT fk_title FOREIGN KEY (title_guid) REFERENCES title(guid) ON DELETE CASCADE,\n" +
//            "    CONSTRAINT fk_note FOREIGN KEY (note_guid) REFERENCES note(guid) ON DELETE CASCADE\n" +
//            ");";
    private Context mContext;

    private MyDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        aCache = ACache.get(context);
    }

    public static synchronized MyDatabaseHelper getInstance(Context context){
        if(instance ==  null){
            instance = new MyDatabaseHelper(context.getApplicationContext(),DATABASE_NAME,null,DATABASE_VERSION);
        }
        return instance;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(create_table_user);
        db.execSQL(create_table_note);
        db.execSQL(create_table_title);
        db.execSQL(create_table_clock);
        //db.execSQL(create_table_note_title);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    //初始化数据表 也添加一下测试数据
    public void initDataBase(SQLiteDatabase db){
        //由于在craete方法中调用，不能使用open自定义的合格方法，直接使用onCreate(SQLiteDatabase db)传过来的参数
        //测试模拟用户信息
        //db.execSQL("insert into user (username,ssn,lastUpdateSSN,lastSyncTime) values(?,?,?,?)",
         //       new String[]{"li","0","0","2023-01-12 11:10"});
        //
        db.execSQL("insert into title (guid,title, parent_id, level, is_open ,is_del, ssn, create_time ,user_guid)" +
                        "values(?,?,?,?,?,?,?,?,?)",
                new String[]{"a711587e-feb3-4aa6-b392-7fcaea99cfa5","生活","","0","0","0","0","2023-01-12 11:12","li"}
                );

    }
    public void open(){
        database = this.getWritableDatabase();
    }
    public void close(){//不进行打开和关闭的频繁操作，资源消耗过高容易报错
        if(database!=null){
            database.close();
        }
    }
    public void beginTransaction(){
        if(database == null){
            database = instance.getWritableDatabase();
        }
        database.beginTransaction();
    }
    public void  setTransactionSuccessful(){
        database.setTransactionSuccessful();//设置事务的标志为True
    }
    public void endTransaction(){
        database.endTransaction();//结束事务。由两种情况：commit（提交），rollback（回滚）
        //可能SQL语句会出现报错，就不会执行endTransaction结束事务，所以要用到try的finally语句必须执行endTransaction结束事务

    }


    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit){
        if(database == null){
            open();
        }
//        query(String table,String []  columns, String selection,String []  selectionArgs,String  groupBy, String having, String orderBy,String  limit)方法各参数的含义：
//
//        table ：表名。相当于select *** from table语句中的table 。如果是多表联合查询，可以用逗号将两个表名分开。
//        columns ：要查询出来的列名。相当于 select  *** from table语句中的 ***部分 。如果是查询多个参数，可以用逗号将两个表名分开。例：new String[]{"name","age","sex"}
//        selection：查询条件子句，相当于select *** from table where && 语句中的&&部分，在条件子句允许使用占位符“?”表示条件值 ，例："name=?,age=?,sex=?"
//
//        selectionArgs ：对应于 selection参数 占位符的值，值在数组中的位置与占位符在语句中的位置必须一致，否则就会有异常。  例：与 new String[]{"lisa","1","女"}
//        groupBy ：相当于 select *** from table where && group by ... 语句中 ... 的部分  ，作用是：将同一列的相同名字的参数合并在一起 例;在name列有两个Jame（name:Jame --salay:100，name:Jame --salay:200）,使用...group by name查询后 只显示一个Jame的集合（name:Jame--salay:300）
//        having ：相当于 select *** from table where && group by ...having %%% 语句中 %%% 的部分， 作用于groupBy的条件，例：havig name>2意思是name列相同参数>2
//        orderBy ：相当于 select  ***from ？？  where&& group by ...having %%% order by@@语句中的@@ 部分，如： personid desc（按person 降序）, age asc（按age升序）;
//        limit：指定偏移量和获取的记录数，相当于select语句limit关键字后面的部分。
//        appendClause(query, " WHERE ", where);
//        appendClause(query, " GROUP BY ", groupBy);
//        appendClause(query, " HAVING ", having);
//        appendClause(query, " ORDER BY ", orderBy);
        return database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

    }
    public List<NoteModel> queryNoteBySQL(){
        List<NoteModel> noteModelList = new ArrayList<>();
        Cursor cursor = database.rawQuery("select * from note where note.is_del =0 order By title_guid" ,null);
        //sqlite 不支持多表查询 拆分
        if(cursor.moveToFirst()){
            do {
                //采用列名获取提高一下辨识度，数字难以分辨是哪个,
                @SuppressLint("Range") NoteModel temp = new NoteModel(
                        cursor.getString(cursor.getColumnIndex("guid")),
                        cursor.getString(cursor.getColumnIndex("theme")),
                        cursor.getString(cursor.getColumnIndex("title_guid")),
                        cursor.getInt(cursor.getColumnIndex("is_del"))==0?false:true,
                        cursor.getInt(cursor.getColumnIndex("ssn")),
                        cursor.getString(cursor.getColumnIndex("create_time")),
                        cursor.getString(cursor.getColumnIndex("update_time")),
                        cursor.getString(cursor.getColumnIndex("user_guid"))
                );
                noteModelList.add(temp);
            }while (cursor.moveToNext());
        }
        return noteModelList;
    }

    public void insertDataByTitleModel(TitleModel titleModel){
        database.execSQL("insert into title(guid,title, parent_id, level, is_open ,is_del, ssn, create_time ,update_time,user_guid) values(?,?,?,?,?,?,?,?,?,?)",
                new String []{titleModel.getGuid(),titleModel.getTitle(),titleModel.getParentID(),titleModel.getLevel().toString(),
                        String.valueOf(titleModel.isOpen()?1:0),String.valueOf(titleModel.getDel()?1:0),titleModel.getSsn().toString(),
                titleModel.getCreateTime(),titleModel.getUpdateTime(),titleModel.getUsername()});
    }
    public void updateTitleDelStateByGUID(List<String> titleGUID){
        Integer ssn = (Integer) aCache.getAsObject("user_ssn");

        SimpleDateFormat smf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDate = new Date();
        // 格式化当前时间
        String update_time = smf.format(currentDate);
        titleGUID.add(0,update_time);
        titleGUID.add(0,ssn.toString());
        ssn++;
        aCache.put("user_ssn",ssn);
        String[] guid = titleGUID.toArray(new String[0]);
        StringBuilder DeleteBuilder = new StringBuilder("update title set is_del=1,ssn=?,update_time=? where guid in (");
        for (int i = 0; i < guid.length; i++) {
            DeleteBuilder.append("?");
            if (i < guid.length - 1) {
                DeleteBuilder.append(", ");
            }
        }
        DeleteBuilder.append(")");
        String update = DeleteBuilder.toString();
        database.execSQL(update,guid);

    }
    public void deleteTitleByGUID(List<String> titleGUID){
       String[] GUID =  titleGUID.toArray(new String[0]);
        // 构建 SQL 查询语句
        StringBuilder queryBuilder = new StringBuilder("DELETE FROM title WHERE guid IN (");
        for (int i = 0; i < GUID.length; i++) {
            queryBuilder.append("?");
            if (i < GUID.length - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(")");

// 执行 SQL 查询语句
        String query = queryBuilder.toString();
        database.execSQL(query, GUID);
    }
    public void insertNoteData(NoteModel noteModel){
        this.beginTransaction();
        try {
            database.execSQL("insert into note(guid,theme , note_content,title_guid, is_del, ssn, create_time ,update_time,user_guid) values(?,?,?,?,?,?,?,?,?)",
                    new String[]{noteModel.getGuid(), noteModel.getTheme(), noteModel.getNote_context(),noteModel.getTitle_guid(), String.valueOf(noteModel.getDel() ? 1 : 0),
                            noteModel.getSsn().toString(), noteModel.getCreateTime(), noteModel.getUpdateTime(), noteModel.getUsername()});
            this.setTransactionSuccessful();
        }
        finally {
            this.endTransaction();
        }
    }
    @SuppressLint("Range")
    public String queryNoteContentByGUID(String guid){
        //148b70b2-9247-4a66-b70d-348582856566
        if(database == null){
            open();
        }
        //Cursor cursor = database.query("note",new String[]{"note_content"},"guid=?",new String[]{guid},null,null,null);
        Cursor cursor = database.rawQuery("select note_content from note where guid=?" ,new String[]{guid});
       // return cursor.getString(cursor.getColumnIndex("note_content"));
        if (cursor != null && cursor.moveToFirst()) {
            String noteContent = cursor.getString(cursor.getColumnIndex("note_content"));
            cursor.close();
            return noteContent;
        } else {
            // 如果没有找到匹配的数据，可以返回 null 或者抛出异常，具体取决于你的需求
            return null;
        }
    }
    public void updateNoteData(NoteModel noteModel){
        database.execSQL("update note set note_content = ?,theme = ?,ssn=?,update_time = ?,title_guid = ? where guid = ?",
                new String[]{noteModel.getNote_context(),noteModel.getTheme(),noteModel.getSsn().toString(),noteModel.getUpdateTime(),
                        noteModel.getTitle_guid(),noteModel.getGuid()});
    }

    public void updateNoteDataIsDelByTitleID(List<String> titleGUID){
        Integer ssn = (Integer) aCache.getAsObject("user_ssn");

        SimpleDateFormat smf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDate = new Date();
        // 格式化当前时间
        String update_time = smf.format(currentDate);
        titleGUID.add(0,update_time);
        titleGUID.add(0,ssn.toString());
        ssn++;
        aCache.put("user_ssn",ssn);
        String[] GUID =  titleGUID.toArray(new String[0]);
        // 构建 SQL 查询语句
        StringBuilder deleteBuilder = new StringBuilder("update note set is_del=1,ssn=?,update_time=? WHERE title_guid IN (");
        for (int i = 0; i < GUID.length; i++) {
            deleteBuilder.append("?");
            if (i < GUID.length - 1) {
                deleteBuilder.append(", ");
            }
        }
        deleteBuilder.append(")");

// 执行 SQL 查询语句
        String delete = deleteBuilder.toString();
        database.execSQL(delete, GUID);
    }

    public void updateNoteDataIsDelByNoteID(List<String> NoteID){
        Integer ssn = (Integer) aCache.getAsObject("user_ssn");

        SimpleDateFormat smf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDate = new Date();
        // 格式化当前时间
        String update_time = smf.format(currentDate);
        NoteID.add(0,update_time);
        NoteID.add(0,ssn.toString());

        ssn++;
        aCache.put("user_ssn",ssn);
        String[] GUID =  NoteID.toArray(new String[0]);
        // 构建 SQL 查询语句
        StringBuilder deleteBuilder = new StringBuilder("update note set is_del=1,ssn=?,update_time=? WHERE guid IN (");
        for (int i = 0; i < GUID.length; i++) {
            deleteBuilder.append("?");
            if (i < GUID.length - 1) {
                deleteBuilder.append(", ");
            }
        }
        deleteBuilder.append(")");

// 执行 SQL 查询语句
        String delete = deleteBuilder.toString();
        database.execSQL(delete, GUID);
    }
    public List<Group.Item> queryAllBacklogList(){
        List<Group.Item> list = new ArrayList<>() ;
        Cursor cursor = database.query("clock",null,"is_del = 0",null,null,null,null);
        if(cursor.moveToFirst()){
            do {
                //采用列名获取提高一下辨识度，数字难以分辨是哪个,
                @SuppressLint("Range") Group.Item temp = new Group.Item(
                        cursor.getString(cursor.getColumnIndex("guid")),
                        cursor.getString(cursor.getColumnIndex("clock_name")),
                        cursor.getString(cursor.getColumnIndex("clock_time")),
                        cursor.getInt(cursor.getColumnIndex("is_checked"))==0?false:true,
                        cursor.getInt(cursor.getColumnIndex("is_repeat"))==0?false:true,
                        cursor.getInt(cursor.getColumnIndex("ssn")),
                        cursor.getInt(cursor.getColumnIndex("is_del"))==0?false:true,
                        cursor.getString(cursor.getColumnIndex("create_time")),
                        cursor.getString(cursor.getColumnIndex("update_time")),
                        cursor.getString(cursor.getColumnIndex("user_guid"))
                );
                list.add(temp);
            }while (cursor.moveToNext());
        }
        return list;
    }
    public void insertBacklog(Group.Item item){
        database.execSQL("insert into clock (guid,clock_name,clock_time,is_checked,is_repeat,ssn,is_del,create_time,update_time,user_guid) values(" +
                "?,?,?,?,?,?,?,?,?,?)",new String[]{item.getUUID(),item.getBacklog_name(),item.getBacklog_time(),String.valueOf(item.isChecked() ? 1 : 0),String.valueOf(item.getRepeat()?1:0),
        item.getSsn().toString(),String.valueOf(item.getDel()?1:0),item.getCreateTime(),item.getUpdateTime(),item.getUserGuid()});
    }
    public void updateBacklog(Group.Item item){
        database.execSQL("update clock set clock_time = ?,clock_name = ?,is_checked=?,is_repeat = ?,ssn=?,update_time=? where guid = ?",
                new String[]{item.getBacklog_time(),item.getBacklog_name(),String.valueOf(item.isChecked()?1:0),
                String.valueOf(item.getRepeat()?1:0),item.getSsn().toString(),item.getUpdateTime(),item.getUUID()});
    }
    public void deleteBacklogByUpdateStateDel(Group.Item item){
        database.execSQL("update clock set ssn=?,is_del=1,update_time = ? where guid = ?" ,new String[]{item.getSsn().toString(),
                item.getUpdateTime(),item.getUUID()});
    }
    public void deleteAllData(){
        this.beginTransaction();
        try{
            database.execSQL("delete from user");
            database.execSQL("delete from note");
            database.execSQL("delete from title");
            database.execSQL("delete from clock");
            this.setTransactionSuccessful();
        }finally {
            this.endTransaction();
        }
    }
    public void deleteAllDataNoUser(){
        this.beginTransaction();
        try{
            database.execSQL("delete from note");
            database.execSQL("delete from title");
            database.execSQL("delete from clock");
            this.setTransactionSuccessful();
        }finally {
            this.endTransaction();
        }
    }
    public void insertUser(User user){
        database.execSQL("insert into  user(username,ssn,lastUpdateSSN,lastSyncTime) values(?,?,?,?) ",new String[]{user.getUsername(),
        user.getSsn().toString(),user.getLastUpdateSSN().toString(),user.getLastSyncTime()});
    }
    @SuppressLint("Range")
    public User queryUser(){
        Cursor cursor =  database.query("user",null,null,null,null,null,null);
         User user = null;
        if(cursor.moveToFirst()){
            user = new User(cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("username")),
                    cursor.getInt(cursor.getColumnIndex("ssn")),
                    cursor.getInt(cursor.getColumnIndex("lastUpdateSSN")),
                    cursor.getString(cursor.getColumnIndex("lastSyncTime")));
        }
        return user;

    }
    public void updateUser(User user){
        database.execSQL("update user set ssn=?,lastUpdateSSN=?,lastSyncTime=? where username=?",new String[]{user.getSsn().toString(),user.getLastUpdateSSN().toString(),user.getLastSyncTime(),user.getUsername()});
    }
    public List<TitleModel> queryAllTitle(){
        List<TitleModel> titleModelList = new ArrayList<>();
        Cursor cursor = query("title",null,
                "is_del=?",new String[]{"0"},null,null,"level ASC",null);
        if(cursor.moveToFirst()){
            do {
                //采用列名获取提高一下辨识度，数字难以分辨是哪个,

                @SuppressLint("Range") TitleModel temp = new TitleModel(
                        cursor.getString(cursor.getColumnIndex("guid")),
                        cursor.getString(cursor.getColumnIndex("title")),
                        cursor.getString(cursor.getColumnIndex("parent_id")),
                        cursor.getInt(cursor.getColumnIndex("level")),
                        cursor.getInt(cursor.getColumnIndex("is_open"))==0?false:true,
                        cursor.getInt(cursor.getColumnIndex("is_del"))==0?false:true,
                        cursor.getInt(cursor.getColumnIndex("ssn")),
                        cursor.getString(cursor.getColumnIndex("create_time")),
                        cursor.getString(cursor.getColumnIndex("update_time")),
                        cursor.getString(cursor.getColumnIndex("user_guid"))
                );
                titleModelList.add(temp);
            }while (cursor.moveToNext());
        }
        return titleModelList;
    }
    public List<TitleModel> queryAllNeedSyncTitle(Integer lastSyncSSN){
        List<TitleModel> titleModelList = new ArrayList<>();
        Cursor cursor = query("title",null,
                "ssn>=?",new String[]{lastSyncSSN.toString()},null,null,"level ASC",null);
        if(cursor.moveToFirst()){
            do {
                //采用列名获取提高一下辨识度，数字难以分辨是哪个,

                @SuppressLint("Range") TitleModel temp = new TitleModel(
                        cursor.getString(cursor.getColumnIndex("guid")),
                        cursor.getString(cursor.getColumnIndex("title")),
                        cursor.getString(cursor.getColumnIndex("parent_id")),
                        cursor.getInt(cursor.getColumnIndex("level")),
                        cursor.getInt(cursor.getColumnIndex("is_open"))==0?false:true,
                        cursor.getInt(cursor.getColumnIndex("is_del"))==0?false:true,
                        cursor.getInt(cursor.getColumnIndex("ssn")),
                        cursor.getString(cursor.getColumnIndex("create_time")),
                        cursor.getString(cursor.getColumnIndex("update_time")),
                        cursor.getString(cursor.getColumnIndex("user_guid"))
                );
                titleModelList.add(temp);
            }while (cursor.moveToNext());
        }
        return titleModelList;
    }
    public List<NoteModel> queryAllNeedSyncNote(Integer lastSyncSSN){
        List<NoteModel> noteModelList = new ArrayList<>();
        Cursor cursor = query("note",null,"ssn>=?",new String[]{lastSyncSSN.toString()},null,null,null,null);
        if(cursor.moveToFirst()){
            do {
                //采用列名获取提高一下辨识度，数字难以分辨是哪个,
                @SuppressLint("Range") NoteModel temp = new NoteModel(
                        cursor.getString(cursor.getColumnIndex("guid")),
                        cursor.getString(cursor.getColumnIndex("theme")),
                        cursor.getString(cursor.getColumnIndex("note_content")),
                        cursor.getString(cursor.getColumnIndex("title_guid")),
                        cursor.getInt(cursor.getColumnIndex("is_del"))==0?false:true,
                        cursor.getInt(cursor.getColumnIndex("ssn")),
                        cursor.getString(cursor.getColumnIndex("create_time")),
                        cursor.getString(cursor.getColumnIndex("update_time")),
                        cursor.getString(cursor.getColumnIndex("user_guid"))
                );
                noteModelList.add(temp);
            }while (cursor.moveToNext());
        }
        return noteModelList;
    }
    public List<Group.Item> queryAllNeedSyncItem(Integer lastSyncSSN){
        List<Group.Item> itemList = new ArrayList<>();
        Cursor cursor = query("clock",null,"ssn>=?",new String[]{lastSyncSSN.toString()},null,null,null,null);
        if(cursor.moveToFirst()){
            do {
                //采用列名获取提高一下辨识度，数字难以分辨是哪个,
                @SuppressLint("Range") Group.Item temp = new Group.Item(
                        cursor.getString(cursor.getColumnIndex("guid")),
                        cursor.getString(cursor.getColumnIndex("clock_name")),
                        cursor.getString(cursor.getColumnIndex("clock_time")),
                        cursor.getInt(cursor.getColumnIndex("is_checked"))==0?false:true,
                        cursor.getInt(cursor.getColumnIndex("is_repeat"))==0?false:true,
                        cursor.getInt(cursor.getColumnIndex("ssn")),
                        cursor.getInt(cursor.getColumnIndex("is_del"))==0?false:true,
                        cursor.getString(cursor.getColumnIndex("create_time")),
                        cursor.getString(cursor.getColumnIndex("update_time")),
                        cursor.getString(cursor.getColumnIndex("user_guid"))
                );
                itemList.add(temp);
            }while (cursor.moveToNext());
        }
        return itemList;
    }
    public void deleteTitleExpireData(Integer lastSyncSSN){
        database.execSQL("delete from title where is_del = 1 and ssn < ?",new String[]{lastSyncSSN.toString()});
    }
    public void deleteNoteExpireData(Integer lastSyncSSN){
        database.execSQL("delete from note where is_del = 1 and ssn< ?",new String[]{lastSyncSSN.toString()});
    }
    public void deleteClockExipreData(Integer lastSyncSSN){
        database.execSQL("delete from clock where is_del = 1 and ssn< ?",new String[]{lastSyncSSN.toString()});
    }
    public void insertTitleList(List<TitleModel> titleModelList){
        if(titleModelList==null){return;}
        database.beginTransaction();
        try {
            for (TitleModel titleModel : titleModelList) {
                insertDataByTitleModel(titleModel);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }
    public void insertNoteList(List<NoteModel> noteModelList){
        if(noteModelList==null) {return;}
        database.beginTransaction();
        try {
            for (NoteModel noteModel : noteModelList) {
                insertNoteData(noteModel);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }
    public void insertClockList(List<Group.Item> itemList){
        if(itemList==null) return;
        database.beginTransaction();
        try {
            for (Group.Item item : itemList) {
                insertBacklog(item);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }
    }

