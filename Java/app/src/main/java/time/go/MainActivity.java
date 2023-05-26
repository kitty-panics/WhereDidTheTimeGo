package time.go;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Show_Call_Log_Data();
    }

    public void Show_Call_Log_Data() {
        // 定义存放最近 5 天时长的字典
        // 呼出类型值: 2, 呼出类型值: 1
        Map<String, List<Integer>> fiveDays = new TreeMap<>();
        for(int i = 0; i < 5; i++) {
            Calendar reduceDay = Calendar.getInstance();
            reduceDay.add(Calendar.DAY_OF_MONTH, - i);
            String formatDay = DateFormat.format("yyyy-MM-dd", reduceDay).toString();
            fiveDays.put(formatDay, new ArrayList<Integer>(), new ArrayList<Integer>());
        }

        // 获取所有通话记录
        String[] columns = new String[] {
            CallLog.Calls.NUMBER,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE
        };
        Uri uri = CallLog.Calls.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, columns, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
        int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);
        int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
        int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);

        // 遍历并将时长存入 fiveDays
        while(cursor.moveToNext()) {
            String number = cursor.getString(numberIndex);
            String duration = cursor.getString(durationIndex);
            String date = cursor.getString(dateIndex);
            String type = cursor.getString(typeIndex);

            long callDate = Long.parseLong(date);
            String callDateStr = DateFormat.format("yyyy-MM-dd", callDate).toString();
            if(fiveDays.containsKey(callDateStr)) {
                if(type.equals("2")) {
                    List<Integer> dailyData = fiveDays.get(callDateStr);
                    dailyData.add(Integer.parseInt(duration));
                } else if(type.equals("1")) {
                    List<Integer> dailyData = fiveDays.get(callDateStr);
                    dailyData.add(Integer.parseInt(duration));
                }
            }
        }
        cursor.close();

        // 计算 fiveDays 中各时长和
        TextView textView = (TextView) findViewById(R.id.id_clinfo);
        textView.setText("\n");
        for(String key : fiveDays.keySet()) {
            List<Integer> dailyData = fiveDays.get(key);
            int sumSec = 0;
            int sumCount = dailyData.size();
            int sumCountOn = 0;
            for(int i = 0; i < sumCount; i++) {
                int callTime = dailyData.get(i);
                sumSec += callTime;
                if (callTime != 0) {
                    sumCountOn++;
                }
            }
            double sumMin = (double) sumSec / 60;
            String splice = "   " + key + ": " + sumCountOn + "/" + sumCount + "次、" + sumSec + "秒(" + String.format("%.2f", sumMin) + "分)\n";
            textView.setTextSize(20);
            textView.setGravity(Gravity.LEFT | Gravity.TOP);
            textView.append(splice);
        }
    }
}
