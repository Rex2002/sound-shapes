package de.dhbw.ui;

import java.io.File;
import java.util.HashMap;

public class ResourceProvider {
    HashMap<String, File> loadedResources;
    public ResourceProvider(){
        loadedResources = new HashMap<>();
    }

    public File getResource(String res){
        if(!loadedResources.containsKey(res)){
            loadedResources.put(res, new File(res));
        }
        return loadedResources.get(res);
    }

    public void deleteResource(String res){
        loadedResources.remove(res);
    }
}
