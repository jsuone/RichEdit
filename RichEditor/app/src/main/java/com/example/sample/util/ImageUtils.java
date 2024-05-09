package com.example.sample.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.example.sample.model.NoteModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageUtils {
static String TAG = "notedata";
    // 保存图片到本地文件系统
    public static String saveImageToStorage(Context context, Bitmap bitmap,String NoteID) {
        ACache aCache = ACache.get(context);
        String filename = "image_"+aCache.getAsString("user_name")+"_"+NoteID+"_" + System.currentTimeMillis() + ".jpg";
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(directory, filename);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            //return file.getAbsolutePath();绝对路径的话可能在不同设备分配位置不同
            return file.getPath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 从本地文件系统加载图片
    public static Bitmap loadImageFromStorage(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }
    // 删除图片文件
    public static void deleteImageFile(String imagePath) {
        // 删除图片文件
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            imageFile.delete();
            Log.d(TAG, "deleteImageFile: 图片删除结果"+imageFile.exists());
        }
    }
    public static void deleteImageFileByNoteID(String NoteID,Context context){
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory != null && directory.isDirectory()) {
            // 获取目录中的文件列表
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 判断文件名是否包含笔记 UUID
                    String[] s = file.getName().split("_");
                    if (s[2].equals(NoteID)) {
                        // 删除文件
                        boolean deleted = file.delete();
                        if (deleted) {
                            // 文件删除成功
                            // 这里可以进行相应的操作
                        } else {
                            // 文件删除失败
                            // 这里可以进行相应的处理
                        }
                    }
                }
            }
        }
    }
    public static void deleteAllImageFile(Context context){
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory != null && directory.isDirectory()) {
            // 获取目录中的文件列表
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    Log.d("ImageUtils",file.getName());
                    // 判断文件名是否包含笔记 UUID
                    file.delete();
                }
            }
        }
    }
    public static Map<String,List<String>> queryImageFileByNoteGUID(Context context, List<NoteModel> noteModelList){
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Map<String,List<String>> images = new HashMap<>();
        if(directory!=null && directory.isDirectory()){
            File[] files = directory.listFiles();
            for (NoteModel noteModel : noteModelList) {
                List<String> fileNames = new ArrayList<>();
                if(files!=null){
                    for (File file : files) {
                        String[] s = file.getName().split("_");
                        if(s[2].equals(noteModel.getGuid())){
                            fileNames.add(file.getName());
                        }
                    }
                }
                //fileNames是0也要塞入，因为有些笔记被删除 附带图片为0 如果不添加进行 服务器依然保留着原本的图片不进行删除 不知道对应图片需要删除
                    images.put(noteModel.getGuid(),fileNames);

            }
        }


        return images;
    }
    public static Map<String,String> getFilePathsByNames(Context context, List<String> imageFileNames) {
        Map <String,String> filePaths =  new HashMap<>();
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory != null && directory.isDirectory()) {
            for (String imageName : imageFileNames) {
                File imageFile = new File(directory, imageName);
                if (imageFile.exists()) {
                    filePaths.put(imageFile.getPath(),imageFile.getName());
                }
            }
        }
        return filePaths;
    }
/*    public static void saveImagesFromMap(Context context, List<String> imageList, ACache aCache) {
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(imageList==null) return;
        if(!directory.exists()){
            directory.mkdirs();
        }
        if (directory != null) {
            for (String imageName : imageList) {
                byte[] imageData = aCache.getAsBinary(imageName);
                File file = new File(directory, imageName);
                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData,0, imageData.length);
                    if (bitmap == null) {
                        // 字节数组中的数据不是有效的图像数据
                        return;
                    } else {
                        // 字节数组转换成功，可以使用 bitmap 对象
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    Log.d(TAG, "saveImagesFromMap: 图片保存成功：" + file.getPath()+file.exists());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "saveImagesFromMap: 图片保存失败：" + imageName);
                }
                //清楚缓存
                aCache.remove(imageName);
            }
        } else {
            Log.e(TAG, "saveImagesFromMap: 外部存储目录为空");
        }
            }*/
public static void saveImagesFromMap(Context context, List<String> imageList, Map<String,Bitmap> redis_imageMap) {
    File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    if(imageList==null) return;
    if(!directory.exists()){
        directory.mkdirs();
    }
    if (directory != null) {
        for (String imageName : imageList) {
            Bitmap imageData = redis_imageMap.get(imageName);
            if(imageData==null){return;}
            File file = new File(directory, imageName);
            try {

                FileOutputStream fos = new FileOutputStream(file);
                imageData.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                Log.d(TAG, "saveImagesFromMap: 图片保存成功：" + file.getPath()+file.exists());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "saveImagesFromMap: 图片保存失败：" + imageName);
            }
            //清楚缓存
        }
        redis_imageMap.clear();
    } else {
        Log.e(TAG, "saveImagesFromMap: 外部存储目录为空");
    }
}


}
