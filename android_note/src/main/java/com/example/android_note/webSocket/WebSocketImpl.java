package com.example.android_note.webSocket;

import com.example.android_note.entity.Item;
import com.example.android_note.entity.NoteModel;
import com.example.android_note.entity.TitleModel;
import com.example.android_note.entity.User;
import com.example.android_note.service.ItemService;
import com.example.android_note.service.NoteService;
import com.example.android_note.service.TitleService;
import com.example.android_note.service.UserService;
import com.example.android_note.util.DateTime;
import com.example.android_note.util.RedisUtil;
import com.example.android_note.util.SyncScheduleTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import icu.xuyijie.base64utils.Base64Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.Session;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

@Slf4j
public class WebSocketImpl implements WebSocket , ApplicationContextAware {
    /**
     * 在线连接数（线程安全）
     */
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    private static ConcurrentHashMap<String, WebSocketSession> SESSION_POOL = new ConcurrentHashMap<>();
    private static ApplicationContext applicationContext;
    private UserService userService;
    private TitleService titleService;
    private NoteService noteService;
    private ItemService itemService;
    private RedisUtil redisUtil;
    public  static ConcurrentHashMap<String,List<Integer>> status = new ConcurrentHashMap<>();//1 启用 0关闭 同步的标志位
   // public  int END = 0;//1 启用 0关闭
   // public  int MODEL = 0;//1  客户端将已经修改的数据传输到服务端 2服务端传数据到客户端 3  没有修改
    private Gson gson = new Gson();
    // 全局 Map 对象，用于暂存图片数据
    private Map<String, byte[]> imageCache = new HashMap<>();
    private String imageName = null;
    @Override
    public void handleOpen(String key,WebSocketSession session) {
        SESSION_POOL.put(key,session);
        List<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(0);
        list.add(0);
        status.put(session.getId(),list);
        userService = WebSocketImpl.applicationContext.getBean(UserService.class);
        redisUtil = WebSocketImpl.applicationContext.getBean(RedisUtil.class);
        titleService = WebSocketImpl.applicationContext.getBean(TitleService.class);
        noteService = WebSocketImpl.applicationContext.getBean(NoteService.class);
        itemService = WebSocketImpl.applicationContext.getBean(ItemService.class);
        int count = connectionCount.incrementAndGet();
        log.info("a new connection opened，current online count：{}", count);
    }
    @Override
    public void handleClose(String key) {//如果是关闭在发生异常或者每次断开连接都会执行 就不能添加清楚缓存之类的

      /*  START = 0;
        END = 0;
        MODEL = 0;*/
        WebSocketSession session = SESSION_POOL.remove(key);

        if(session!=null){
            try {
                boolean s = session.isOpen();
                initInEnd(session);
                status.remove(session.getId());
                session.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        int count = connectionCount.decrementAndGet();
        log.info("a new connection closed，current online count：{}", count);
    }
    @Override
    public void handleError(WebSocketSession session, Throwable error) {
     /*   START = 0;
        END = 0;
        MODEL = 0;*/
        changeStatus(session,0,0,0);
        SESSION_POOL.remove( (String) session.getAttributes().get("token"));

        if(session!=null){
            //try {
                initInEnd(session);
            status.remove(session.getId());
                //session.close();
            /*}catch (IOException e) {
                e.printStackTrace();
            }*/
        }

        log.error("websocket error：{}，session id：{}", error.getMessage(), session.getId());
        log.error("", error);
    }

    @Override
    public void handleMessage(WebSocketSession session, String message) throws IOException {
        // 只处理前端传来的文本消息，并且直接丢弃了客户端传来的消息
        log.info("received a message：{}", message);
        //将json格式数据转换成实体对象 如果code是1 0 0 表示首先传入的是用户表的数据 进行判断之后的同步流程
        if(message.startsWith("sync_flag_")){
           String[] messageArr = message.split("_");
            updateSyncFlag(messageArr[2],messageArr[3],messageArr[4],session);
        }
        if(message.startsWith("sync_content_")){
            String content = message.substring("sync_content_".length());
                parseSyncContent(content,session);

        }
        //code是101 进行数据同步
        //比较用户表与客户端的上次同步序列号和同步时间
        //如果
    }
    @Override
    public void handleMessage(WebSocketSession session, ByteBuffer byteBuffer){
        System.out.println("接收到字节消息");
        List<Integer> list = getStatus(session);
        if(list.get(0)==1&&list.get(1)!=1){
            if(imageName==null){
               // byteBuffer.flip(); // 切换到读模式

                // 创建 CharBuffer
                CharBuffer charBuffer = Charset.forName("UTF-8").decode(byteBuffer);

                // 转换为字符串
                imageName = charBuffer.toString();

            }else{
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                imageCache.put(imageName, bytes);
                imageName = null;
                //一组图片数据存储完毕
            }
            //String workingDirectory = System.getProperty("user.dir")+"\\src\\main\\resources\\static";
        }

    }

    @Override
    public void sendMessage(WebSocketSession session, String message) throws IOException {

        this.sendMessage(session, new TextMessage(message));
    }

    @Override
    public void sendMessage(String token, TextMessage message) throws IOException {
        WebSocketSession session = SESSION_POOL.get(token);
        if (session != null && session.isOpen()) {
            this.sendMessage(session, message);
        }
    }

    @Override
    public void sendMessage(String token, String message) throws IOException {
        this.sendMessage(token, new TextMessage(message));
    }

    @Override
    public void sendMessage(WebSocketSession session, TextMessage message) throws IOException {
        session.sendMessage(message);
    }

    @Override
    public void broadCast(String message) throws IOException {

    }

    @Override
    public void broadCast(TextMessage message) throws IOException {

    }




    public static ConcurrentHashMap<String, WebSocketSession> getSessions() {
        return SESSION_POOL;
    }

    @Override
    public int getConnectionCount() {
        return connectionCount.get();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        WebSocketImpl.applicationContext = applicationContext;
    }
    private void updateSyncFlag(String start,String end,String model,WebSocketSession  session)  {


       int  START = Integer.parseInt(start);
       int  END = Integer.parseInt(end);
       int  MODEL = Integer.parseInt(model);
        changeStatus(session,START,END,MODEL);
        if(START==1&&END==1){
            //同步完成
            if(MODEL==1){
                try {
                    session.sendMessage(new TextMessage("response_content_"+"结束同步"));
                    session.sendMessage(new PingMessage(ByteBuffer.wrap("ping".getBytes())));
                } catch (IOException e) {
                    throw new RuntimeException("同步失败啦");
                }
                     SyncScheduleTask.scheduledExecutorService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("执行一次同步 session_id"+session.getId());
                            if(session==null){
                                SyncScheduleTask.connectionStatus.remove(session.getId());
                                return;
                            }
                            if(!session.isOpen()){
                                SyncScheduleTask.connectionStatus.remove(session.getId());
                                return;
                            }
                            String futureName = SyncScheduleTask.connectionStatus.get(session.getId());
                            if(futureName==null){
                                return;
                            }

                            User user = (User) redisUtil.get(session.getId());
                            user.setLastUpdateSSN(user.getSsn());
                            //同步时间已经更新了
                            //方便客户端触发同步结束回调处理 同时由于如果这句报错说明消息发送失败 客户端不会进行同步结束处理 服务器的模式也未进入同步结束 直接清除所有同步缓存数据
                            //进行数据的处理 该删除的删除 该更新插入的进行相应操作
                            List<TitleModel> titleModelList = (List<TitleModel>) redisUtil.get("sync_title_"+session.getId());
                            List<NoteModel> noteModelList = (List<NoteModel>) redisUtil.get("sync_note_"+session.getId());
                            List<Item> itemList = (List<Item>) redisUtil.get("sync_clock_"+session.getId());
                            if(titleModelList!=null&&titleModelList.size()!=0){
                                syncData(titleModelList);
                            }
                            if(noteModelList!=null&&noteModelList.size()!=0){
                                syncData(noteModelList);
                            }
                            if(itemList!=null&&itemList.size()!=0){
                                syncData(itemList);
                            }

                            //将数据同步到数据库
                            userService.updateUserInfo(user);
                            //将图片数据进行保存 对需要删除的图片进行删除
                            List<String> removeImageList = (List<String>) redisUtil.get("sync_image_remove_"+session.getId());
                            String imagePath = "src/main/resources/static/images/"+user.getUsername();
                            if(removeImageList!=null&&removeImageList.size()!=0){
                                for (String s : removeImageList) {
                                    File file = new File(imagePath,s);
                                    if(file.exists()){
                                        file.delete();
                                        System.out.println("删除文件结果"+file.delete());
                                    }
                                }
                            }
                            if(imageCache.size()!=0){
                                for (Map.Entry<String, byte[]> entry : imageCache.entrySet()) {
                                    String imageName = entry.getKey();
                                    byte[] imageBytes = entry.getValue();

                                    File file = new File(imagePath,imageName);
                                    System.out.println("文件位置"+file.getAbsolutePath());
                                    if(!file.getParentFile().exists()){
                                        file.getParentFile().mkdirs();
                                    }
                                    FileOutputStream fos = null;
                                    try {
                                        fos = new FileOutputStream(file);
                                        fos.write(imageBytes);
                                        fos.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                            imageCache.clear();
                            imageName = null;
                            //将数据同步到redis
                            redisUtil.delete(session.getId());
                            redisUtil.delete("sync_title_"+session.getId());
                            redisUtil.delete("sync_note_"+session.getId());
                            redisUtil.delete("sync_clock_"+session.getId());
                            redisUtil.delete("sync_image_remove_"+session.getId());
                            SyncScheduleTask.connectionStatus.remove(session.getId());
                            System.out.println("结束执行一次同步");
                        }

                    },200,TimeUnit.MILLISECONDS);

                //initInEnd(session);
            }
            if(MODEL==2){
                //客户端传来数据接收完毕 同步结束阶段 标志位初始化 发送响应消息触发客户端的数据插入
                try {
                    session.sendMessage(new TextMessage("response_content_"+START+"_"+END+"_"+MODEL));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            changeStatus(session,0,0,0);
        }
    }
    private void parseSyncContent(String content,WebSocketSession session) throws IOException {//解析客户端传递过来的信息并返回服务端的响应

        List<Integer> list = getStatus(session);
        int START = list.get(0);
        int END = list.get(1);
        int MODEL = list.get(2);

        if(START==1&&END!=1){
            //同步未完成
            if(MODEL==0){
                //表示需要确定是哪种模式 查取用户数据进行比对
                User user = gson.fromJson(content, User.class);
                User userInDataBase = userService.queryUserByName(user.getUsername());
                String currentTime = DateTime.getCurrentTime();

                if(userInDataBase!=null){
                    //表示用户存在
                    if((userInDataBase.getLastUpdateSSN()==user.getLastUpdateSSN())&&(userInDataBase.getLastSyncTime().equals(user.getLastSyncTime()))){
                        //表示本地数据是最新数据
                        if(user.getSsn()>user.getLastUpdateSSN()){
                            //表本地需要上传修改
                            MODEL = 1;//客户端传输数据到服务端
                            changeStatus(session,START,END,MODEL);
                            userInDataBase.setLastSyncTime(currentTime);
                            session.sendMessage(new TextMessage( "response_content_"+MODEL+"_"+gson.toJson(userInDataBase))); ;//响应加入下一步模式是为了方便处理内容
                            session.sendMessage(new TextMessage("response_flag_"+START+"_"+END+"_"+MODEL));//先发送响应消息 客户端在接受之前模式的响应进行处理 再发送响应模式信息 变换模式
                            //本地才是最新 但由于本地用户信息对于服务器来说有所缺失，比如用户密码就没有保存在本地 需要补上
                            user.setPassword_change_time(userInDataBase.getPassword_change_time());
                            user.setPassword(userInDataBase.getPassword());
                            user.setLastSyncTime(currentTime);
                            redisUtil.set(session.getId(),user);

                       } else if (user.getSsn()== user.getLastUpdateSSN()) {
                            //没有修改
                            MODEL = 3;
                            END = 1;
                            log.info("没有修改");
                            changeStatus(session,START,END,MODEL);
                            session.sendMessage(new TextMessage( "response_content_"+MODEL+"_"+gson.toJson(userInDataBase)));
                            session.sendMessage(new TextMessage("response_flag_"+START+"_"+END+"_"+MODEL));
                            initInEnd(session);

                        }
                    } else if (userInDataBase.getLastUpdateSSN()>user.getLastUpdateSSN()) {
                        //服务器数据最新 说明是重新登录或者新设备登录或者旧设备登录但是账户在其他新设备抢占后，有了更新，需要以最新的为准
                        MODEL = 2;
                        changeStatus(session,START,END,MODEL);
                        //userInDataBase.setLastSyncTime(currentTime);
                        session.sendMessage(new TextMessage( "response_content_"+MODEL+"_"+gson.toJson(userInDataBase)));
                        session.sendMessage(new TextMessage("response_flag_"+START+"_"+END+"_"+MODEL));
                        //userInDataBase.setLastSyncTime(currentTime);
                        redisUtil.set(session.getId(),userInDataBase);
                        //发送对应用户的所有笔记标题 提醒数据 以及图片
                        sendCloudInfoList(session);
                        //进入结束阶段
                       /* END = 1;
                        changeStatus(session,START,END,MODEL);
                        session.sendMessage(new TextMessage("response_flag_"+START+"_"+END+"_"+MODEL));
                        initInEnd(session);*/

                    }else{
                        MODEL = 3;
                        END = 1;
                        log.info("没有修改");
                        changeStatus(session,START,END,MODEL);
                        session.sendMessage(new TextMessage( "response_content_"+MODEL+"_"+gson.toJson(userInDataBase)));
                        session.sendMessage(new TextMessage("response_flag_"+START+"_"+END+"_"+MODEL));
                        initInEnd(session);
                        System.out.println("出现三种情况之外的异常");
                    }
                }
                //将客户端的user对象同步时间更新并且加入密码 进行保存到缓存 并将同步时间返回给客户端保存 如果同步成功就插入数据库


                return;
            }else if(MODEL==1){//本地上传的数据
                String[] contentArr = content.split("_");
                String json = null;
                switch (contentArr[0]){//解析传递过来的数据类型
                    case "title":
                        json = content.substring("title_".length());
                        if(json!=null||!json.equals("")){
                            List<TitleModel> titleModelList = gson.fromJson(json, new TypeToken<List<TitleModel>>() {}.getType());
                            redisUtil.set("sync_title_"+session.getId(),titleModelList);
                        }

                        break;
                    case "note":
                        json = content.substring("note_".length());
                        if(json!=null||!json.equals("")){
                            List<NoteModel> noteModelList = gson.fromJson(json, new TypeToken<List<NoteModel>>() {}.getType());
                            redisUtil.set("sync_note_"+session.getId(),noteModelList);
                        }
                        break;
                    case "clock":
                        json = content.substring("clock_".length());
                        if(json!=null||!json.equals("")){
                            List<Item> clockModelList = gson.fromJson(json, new TypeToken<List<Item>>() {}.getType());
                            redisUtil.set("sync_clock_"+session.getId(),clockModelList);
                        }
                        break;
                    case "image":
                        json = content.substring("image_".length());
                       // List<String> imageList = gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
                        Map<String,List<String>> imageList = gson.fromJson(json, new TypeToken<Map<String,List<String>>>() {}.getType());
                        //比较后返回需要上传的图片 并存储需要被删除的图片
                        User user = (User) redisUtil.get(session.getId());
                        Map<String,List<String>> imageListMap = getCloudImageList(user.getUsername());
                        List<String> uploadList = new ArrayList<>();
                        List<String> removeList = new ArrayList<>();
                        imageList.forEach((key,value)->{
                            List<String> cloudImageList = imageListMap.get(key);
                            List<String> cloudImageRemoveList = new ArrayList<>();
                            List<String> imageListUploadList = new ArrayList<>();
                            if(cloudImageList!=null&&cloudImageList.size()>0){
                                cloudImageRemoveList.addAll(cloudImageList);
                            }
                            if (value!=null&&value.size()>0) {
                                imageListUploadList.addAll(value);//参数不能为空否则会报错
                            }
                            if(cloudImageList!=null&&cloudImageList.size()>0){
                                imageListUploadList.removeAll(cloudImageList);//客户端需要上传的图片

                            }
                            if (value!=null&&value.size()>0) {
                                cloudImageRemoveList.removeAll(value);//服务端需要被删除的图片
                            }
                            if(imageListUploadList.size()>0){
                                uploadList.addAll(imageListUploadList);
                            }
                            if(cloudImageRemoveList.size()>0){
                                removeList.addAll(cloudImageRemoveList);
                            }

                        });
                        session.sendMessage(new TextMessage("response_content_"+gson.toJson(uploadList)));

                        redisUtil.set("sync_image_remove_"+session.getId(),removeList);
                        break;
                    default:
                        break;

                }
                return ;
            }

        }

        }
        public <T> void syncData(List<T> data){
        if(data.get(0) instanceof TitleModel){
            List<TitleModel> titleModelList = (List<TitleModel>) data;
            Iterator it = titleModelList.iterator();
            while (it.hasNext()){
                TitleModel titleModel = (TitleModel) it.next();
                if(titleModel.getIsDel()){
                    //表示需要删除
                    int raws = titleService.deleteTitleByGUID(titleModel.getGuid());
                    if(raws==0){
                        //表示数据库中不存在该数据 是本地在同步之后的数据中被删除的
                        it.remove();
                    }
                }else {
                    //表示需要插入或者更新
                    int raws = titleService.updateTitleByGUID(titleModel);
                    if(raws==0){
                        //表示数据库中不存在该数据 是本地的同步之后的新数据
                        titleService.insertTitle(titleModel);
                    }
                }
        }
        }else if(data.get(0) instanceof NoteModel){
            List<NoteModel> noteModelList = (List<NoteModel>) data;
            Iterator it = noteModelList.iterator();
            while (it.hasNext()){
                NoteModel noteModel = (NoteModel) it.next();
                if(noteModel.getIsDel()){
                    //表示需要删除
                    int raws = noteService.deleteNoteByGUID(noteModel.getGuid());
                    if(raws==0){
                        //表示数据库中不存在该数据 是本地的同步之后的数据中被删除的
                        it.remove();
                    }
                }else {
                    //表示需要插入或者更新
                   int raws = noteService.updateNoteByGUID(noteModel);
                   if(raws==0){
                       //表示数据库中不存在该数据 是本地的同步之后的新数据
                       noteService.insertNote(noteModel);
                   }
               }
           }
        } else if (data.get(0) instanceof Item) {
            List<Item> itemList = (List<Item>) data;
            Iterator it = itemList.iterator();
            while (it.hasNext()){
                Item item = (Item) it.next();
                if(item.getIsDel()){
                    //表示需要删除
                    int raws = itemService.deleteItemByGUID(item.getUUID());
                    if(raws==0){
                        //表示数据库中不存在该数据 是本地的同步之后的数据中被删除的
                        it.remove();
                    }
                }else {
                    //表示需要插入或者更新
                    int raws= itemService.updateItem(item);
                    if(raws==0){
                        //表示数据库中不存在该数据 是本地的同步之后的新数据
                        itemService.insertItem(item);
                    }
                }
            }
        }

        }
        public Map<String, List<String>> getCloudImageList(String username){
            String imagePath = "src/main/resources/static/images/"+username;
            File file = new File(imagePath);
            if(!file.exists()){
                file.mkdirs();
            }
            Map<String, List<String>> imageMap = new HashMap<>();
            File[] files = file.listFiles();
            for (File file1 : files) {
               String[] ss =  file1.getName().split("_");
               String key = ss[2];
               if(imageMap.containsKey(key)){
                   imageMap.get(key).add(file1.getName());
               }else{
                   List<String> newList = new ArrayList<>();
                   newList.add(file1.getName());
                   imageMap.put(key,newList);
               }
            }
            return imageMap;
        }
        public void sendCloudInfoList(WebSocketSession session){
        ////发送对应用户的所有笔记标题 提醒数据 以及图片
            User user = (User) redisUtil.get(session.getId());
            List<NoteModel> noteModelList = noteService.selectNoteListByUserName(user.getUsername());
            List<TitleModel> titleModelList = titleService.selectTitleListByUserName(user.getUsername());
            List<Item> itemList = itemService.selectClockListByUserName(user.getUsername());

            try {
                session.sendMessage(new TextMessage("response_content_note_"+gson.toJson(noteModelList)));
                session.sendMessage(new TextMessage("response_content_title_"+gson.toJson(titleModelList)));
                session.sendMessage(new TextMessage("response_content_clock_"+gson.toJson(itemList)));
                sendCloudImageList(session, user.getUsername());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        public void sendCloudImageList(WebSocketSession session,String userName) throws IOException {
            List<String> imageList = new ArrayList<>();
            String imagePath = "src/main/resources/static/images/"+userName;
            File directory = new File(imagePath);
            if(directory.exists()){
                directory.mkdirs();
            }
            FileInputStream fileInputStream;
            BufferedInputStream bufferedInputStream;

                File[] files = directory.listFiles();
                for (File file : files) {

                    //fileInputStream = new FileInputStream(file);
                  /*  bufferedInputStream = new BufferedInputStream(fileInputStream);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    int len = bufferedInputStream.read();
                    byte[] bytes = new byte[1024];
                    while ((len = bufferedInputStream.read(bytes)) != -1) {
                        Base64.Decoder (bytes);
                        byteArrayOutputStream.write(bytes, 0, len);
                    }*/
                    imageList.add(file.getName());
                    //String base64Image = Base64Utils.transferToBase64(file,false);
                    //session.sendMessage(new TextMessage("response_content_imageName_"+file.getName()));
                    //session.sendMessage(new TextMessage("response_content_image_"+base64Image));
                    /*fileInputStream.close();
                    bufferedInputStream.close();
                    byteArrayOutputStream.close();*/
                }
            session.sendMessage(new TextMessage("response_content_imageName_"+gson.toJson(imageList)));
        }
        public void initInEnd(WebSocketSession session){
            imageCache.clear();
            imageName = null;
            //将数据同步到redis
            redisUtil.delete(session.getId());
            redisUtil.delete("sync_title_"+session.getId());
            redisUtil.delete("sync_note_"+session.getId());
            redisUtil.delete("sync_clock_"+session.getId());
            redisUtil.delete("sync_image_remove_"+session.getId());
            changeStatus(session,0,0,0);
        }
        public void changeStatus(WebSocketSession session,int start,int end,int model){
           List list =status.get(session.getId());
           if(list!=null){
               list.set(0,start);
               list.set(1,end);
               list.set(2,model);
               status.put(session.getId(),list);
           }else{
               log.info("changeStatus error");
           }

        }
        public List<Integer> getStatus(WebSocketSession session){
            List<Integer> list = status.get(session.getId());
            if(list!=null){
                return list;
            }else{
                log.info("getStatus error");
                return null;
            }
        }

}
