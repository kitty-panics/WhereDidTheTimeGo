# -*- coding: utf-8 -*-

#
# 统计最近 10 天呼出、呼入时长
#

from androidhelper import sl4a
from datetime import datetime, timedelta
import time

# 获取所有通话记录
pydroid = sl4a.Android()
uricontent = pydroid.getConstants("android.provider.CallLog$Calls").result
calllog = pydroid.queryContent(uricontent['CONTENT_URI'], ["number", "duration", "date", "type"]).result

# 定义存放最近 10 天呼出、呼入时长的字典
# 呼出类型值: 2、呼入类型值: 1
ten_days = {}
for i in range(0, 10):
    reduce_day = datetime.now() - timedelta(days = i)
    format_day = reduce_day.strftime('%Y-%m-%d')
    ten_days[format_day] = [[], []]

# 遍历通话记录，并将呼出、呼入时长存入字典
# 呼出类型值: 2、呼入类型值: 1
calllog_len = len(calllog)
for i in range(0, calllog_len):
    calllog_info = calllog[i]
    call_time = calllog_info['duration']
    call_date = time.strftime("%Y-%m-%d", time.localtime(int(calllog_info['date']) / 1000))
    call_type = calllog_info['type']
    if call_date in ten_days:
        if call_type == '2':
            ten_days[call_date][0].append(int(call_time))
        elif call_type == '1':
            ten_days[call_date][1].append(int(call_time))

# 计算出最近 10 天的呼出、呼入时长
for i in ten_days:
    # 呼出
    out_daily_data = ten_days[i][0]
    out_sum_sec = sum(out_daily_data)
    out_sum_min = round(out_sum_sec / 60, 2)
    out_sum_count = len(out_daily_data)
    out_tmp_list = [j for j in out_daily_data if j != 0]
    out_sum_count_on = len(out_tmp_list)
    # 呼入
    in_daily_data = ten_days[i][1]
    in_sum_sec = sum(in_daily_data)
    in_sum_min = round(in_sum_sec / 60, 2)
    in_sum_count = len(in_daily_data)
    in_tmp_list = [j for j in in_daily_data if j != 0]
    in_sum_count_on = len(in_tmp_list)
    print(i + ': 出: ' + str(out_sum_count_on) + '/' + str(out_sum_count) + '次、' + str(out_sum_sec) + '秒(' + str(out_sum_min) + '分)')
    print('            入: ' + str(in_sum_count_on) + '/' + str(in_sum_count) + '次、' + str(in_sum_sec) + '秒(' + str(in_sum_min) + '分)')
