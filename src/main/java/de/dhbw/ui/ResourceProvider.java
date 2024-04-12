package de.dhbw.ui;

import java.io.File;
import java.util.HashMap;

public class ResourceProvider {
    final HashMap<String, File> loadedResources;
    public ResourceProvider(){
        loadedResources = new HashMap<>();
    }

    public File getResource(String res){
        if(!loadedResources.containsKey(res)){
            loadedResources.put(res, new File(res));
        }
        return loadedResources.get(res);
    }
}
