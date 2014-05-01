package net.sf.supercollider.android;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import android.content.Context;
import java.util.Map;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.json.JSONException;
import android.util.Log;

public class JSONPersister {
    private JSONObject json;
    private String fileName;
    private Context context;

    public JSONPersister(Context context, JSONObject json, String fileName) {
        this.context = context;
        this.json = json;
    }

    public JSONPersister(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
    }

    public void setJSONObject(JSONObject json) {
        this.json = json;
    }

    public JSONObject getJSONObject() {
        return json;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getJSONString() {
        return this.json.toString();
    }

    public void persist() throws IOException {
        FileWriter writer = new FileWriter(new File(context.getFilesDir(), fileName));
        writer.write(this.json.toString());
        writer.flush();
        writer.close();
    }

    public void load() throws FileNotFoundException, IOException, JSONException {
        FileInputStream fis = context.openFileInput(fileName);
        StringBuffer fileContent = new StringBuffer("");
        byte[] buffer = new byte[1024];
        int n;

        while ((n = fis.read(buffer)) != -1) 
        { 
            fileContent.append(new String(buffer, 0, n)); 
        }

        this.json = new JSONObject(fileContent.toString());
    }
}
