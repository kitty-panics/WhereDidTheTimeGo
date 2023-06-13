package where.did.the.time.go;

import android.Manifest;
import android.database.Cursor;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.permissionx.guolindev.PermissionX;

import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;
import java.util.stream.IntStream;

public class MainActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        PermissionX.init(this)
            .permissions(Manifest.permission.READ_CALL_LOG)
            .request((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    Show_Call_Log_Data();
                } else {
                    Toast.makeText(this, "为确保软件功能的正常使用\n需允许 '读取通话记录' 权限", Toast.LENGTH_LONG).show();
                    this.finish();
                }
            });
    }

    public void Show_Call_Log_Data() {
        // 定义存放最近 10 天时长的字典
        TreeMap<String, int[][]> tenDays = new TreeMap<>();
        IntStream.range(0, 10).forEach(i -> {
            Calendar reduceDay = Calendar.getInstance();
            reduceDay.add(Calendar.DAY_OF_MONTH, -i);
            String formatDay = DateFormat.format("yyyy-MM-dd", reduceDay).toString();
            tenDays.put(formatDay, new int[][]{{}, {}});
        });

        // 获取所有通话记录
        String[] columns = new String[]{
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
        // 遍历并将时长存入 tenDays
        while (cursor.moveToNext()) {
            String number = cursor.getString(numberIndex);
            int duration = cursor.getInt(durationIndex);
            long date = cursor.getLong(dateIndex);
            int type = cursor.getInt(typeIndex);

            Date dateTime = new Date(date);
            SimpleDateFormat formatDay = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDay = formatDay.format(dateTime);
            // 呼出类型值: 2, 呼出类型值: 1
            if (tenDays.containsKey(formattedDay)) {
                if (type == CallLog.Calls.OUTGOING_TYPE) {
                    int[][] dailyData = tenDays.get(formattedDay);
                    int[] outDailyData = dailyData[0];
                    int[] outTmpList = new int[outDailyData.length + 1];
                    System.arraycopy(outDailyData, 0, outTmpList, 0, outDailyData.length);
                    outTmpList[outDailyData.length] = duration;
                    dailyData[0] = outTmpList;
                } else if (type == CallLog.Calls.INCOMING_TYPE) {
                    int[][] dailyData = tenDays.get(formattedDay);
                    int[] inDailyData = dailyData[1];
                    int[] inTmpList = new int[inDailyData.length + 1];
                    System.arraycopy(inDailyData, 0, inTmpList, 0, inDailyData.length);
                    inTmpList[inDailyData.length] = duration;
                    dailyData[1] = inTmpList;
                }
            }
        }
        cursor.close();

        // 计算 tenDays 中各时长和
        TextView textView = (TextView) findViewById(R.id.id_clinfo);
        textView.setText("\n");
        for (String day : tenDays.keySet()) {
            int[][] dailyData = tenDays.get(day);
            int[] outDailyData = dailyData[0];
            int outSumSec = 0;
            int outSumCount = 0;
            int outSumCountOn = 0;
            for (int duration : outDailyData) {
                outSumSec += duration;
                outSumCount++;
                if (duration != 0) {
                    outSumCountOn++;
                }
            }
            double outSumMin = Math.round(outSumSec / 60.0 * 100.0) / 100.0;

            int[] inDailyData = dailyData[1];
            int inSumSec = 0;
            int inSumCount = 0;
            int inSumCountOn = 0;
            for (int i = 0; i < inDailyData.length; i++) {
                int duration = inDailyData[i];
                inSumSec += duration;
                inSumCount++;
                if (duration != 0) {
                    inSumCountOn++;
                }
            }
            double inSumMin = Math.round(inSumSec / 60.0 * 100.0) / 100.0;

            String splice = " " + day + ": 出: "                                           +
                            outSumCountOn + "/" + outSumCount + "次、"                     +
                            outSumSec + "秒(" + String.format("%.2f", outSumMin) + "分)\n" +
                            "             入: "                                            +
                            inSumCountOn + "/" + inSumCount + "次、"                       +
                            inSumSec + "秒(" + String.format("%.2f", inSumMin) + "分)\n";

            textView.setGravity(Gravity.LEFT | Gravity.TOP);
            textView.setTextSize(16);
            textView.setTypeface(Typeface.MONOSPACE);
            textView.append(splice);
        }
    }
}