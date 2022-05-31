package com.example.str

import android.graphics.Rect

data class Predict(var bbox : Rect, var label : String, var maskLabel : String = "" )