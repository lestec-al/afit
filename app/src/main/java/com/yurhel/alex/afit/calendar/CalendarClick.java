package com.yurhel.alex.afit.calendar;

import com.yurhel.alex.afit.core.Obj;

import java.util.ArrayList;
import java.util.Date;

public interface CalendarClick {
    void onClick(Date calendar, ArrayList<Obj> dataThisDay);
}