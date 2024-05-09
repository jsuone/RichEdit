package com.example.sample.util;

import com.example.sample.Impl.Observer;
import com.example.sample.model.TitleModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private Map<String, Observer> observersMap;
    private Map<String, TitleModel> dataMap;
    private static DataManager dataManager;
    private DataManager() {
        observersMap = new HashMap<>();
        dataMap = new HashMap<>();
    }
    public static DataManager getInstance(){
        if(dataManager==null){
            dataManager = new DataManager();
        }
        return dataManager;
    }
    // 注册观察者
    public <T> void registerObserver(String dataType, Observer<T> observer) {
            observersMap.put(dataType, observer);
    }

    // 注销观察者
    public <T> void unregisterObserver(String dataType, Observer<T> observer) {
        Observer observers = observersMap.get(dataType);
        if (observers != null) {
            observersMap.remove(dataType);
        }
    }

    // 通知观察者数据变化
    public  <T> void notifyObservers(String dataType, List<T> newData) {
/*        if(newData.size()!=0&&newData.get(0) instanceof TitleModel){不在这里获取，在数据变化时在添加进去
            dataMap.put(dataType, (TitleModel) newData.get(0));
        }*/
        Observer observer = observersMap.get(dataType);
        if(observer!=null){
            observer.onDataChanged(newData);
        }

    }
    public  void addTitleData(String dataType,TitleModel newData){
        dataMap.put(dataType, newData);
    }
    // 获取数据
    public TitleModel getTitleData(String dataType) {
        TitleModel model = dataMap.get(dataType);
        return model;
    }
    public String getTitlePathByTitleGUID(String targetGuid) {

        TitleModel temp = getTitleModelByParentID(dataMap.get(Constant.Title_KEY),targetGuid);

        if(temp.getParentID()==null||temp.getParentID().equals("")){
            return temp.getTitle();

        }
        return getTitlePathByTitleGUID(temp.getParentID())+"/"+temp.getTitle();

    }
    private TitleModel getTitleModelByParentID(TitleModel titleModel,String guid){
        if(titleModel.getGuid().equals(guid)){
            return titleModel;
        }
        if(titleModel.getChild()!=null){
            List<TitleModel> models = titleModel.getChild();
            for (TitleModel model : models) {
                TitleModel temp =  getTitleModelByParentID(model,guid);
                if(temp!=null){return temp;}
            }
        }
        return null;
    }
}


