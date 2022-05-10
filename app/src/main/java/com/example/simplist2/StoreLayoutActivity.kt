package com.example.simplist2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class StoreLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_layout)

        updateStoreLayoutView()

        val addButton: Button = findViewById(R.id.store_layout_add_button)
        addButton.setOnClickListener {
            addDepartment()
        }

        val deleteButton: Button = findViewById(R.id.store_layout_delete_button)
        deleteButton.setOnClickListener {
            deleteDepartment()
        }
    }

    private fun addDepartment () {
        //var departmentCount = sharedPref.getInt("count", 0)
        val sharedPref = getSharedPreferences("store_Layout", Context.MODE_PRIVATE)
        var editor = sharedPref.edit()
        var departmentCount = sharedPref.getInt("departmentCount", 0)


        //get department name from user entry
        val departmentName_editText: EditText = findViewById(R.id.store_layout_dep_name_editText)
        var departmentName = departmentName_editText.text.toString()

        //get order number from user entry
        val orderNumber_editText: EditText = findViewById(R.id.store_layout_order_editText)
        var orderNumber = orderNumber_editText.text.toString().toIntOrNull()
        if (orderNumber == null) {
            orderNumber = departmentCount + 1
        }

        var maxAllowedOrderNumber = departmentCount + 1

        //save in shared preferences

        if(orderNumber < 1){
            Toast.makeText(this, "Order must be greater than 0", Toast.LENGTH_SHORT).show()
        }

        else if (orderNumber > maxAllowedOrderNumber){
            Toast.makeText(this, "Order cannot be more than $maxAllowedOrderNumber", Toast.LENGTH_SHORT).show()
        }

        else {
            //check if it is being added to the end of the list
            if(orderNumber == maxAllowedOrderNumber) {
                editor.putString(orderNumber.toString(), departmentName.toString())
                editor.apply()
                departmentCount = departmentCount + 1
                Log.d("message", "saved in preferences: " + sharedPref.getString("1", ""))
            }
            //if not we have to shift the list so we can insert the department at the correct location and preserve the order
            else{
                for(i in departmentCount downTo orderNumber){
                    var tempName = sharedPref.getString(i.toString(), "")
                    editor.putString((i+1).toString(), tempName)
                    editor.apply()
                }
                editor.putString(orderNumber.toString(), departmentName.toString())
                editor.apply()
                departmentCount = departmentCount + 1
            }
        }

        editor.putInt("departmentCount", departmentCount)
        editor.apply()
        updateStoreLayoutView()
        departmentName_editText.text.clear()
        orderNumber_editText.text.clear()
    }

    private fun updateStoreLayoutView () {
        val sharedPref = getSharedPreferences("store_Layout", Context.MODE_PRIVATE)
        var editor = sharedPref.edit()
        var departmentCount = sharedPref.getInt("departmentCount", 0)

        //var departmentCount = sharedPref.getInt("count", 0)
        val storeLayoutTextView: TextView = findViewById(R.id.store_layout_textView)
        var storeLayout:String = ""
        for (i in 1..departmentCount){
            var name = sharedPref.getString(i.toString(), "")
            storeLayout = storeLayout.plus(i.toString()).plus(". ").plus(name).plus("\n")
        }

        storeLayoutTextView.text = storeLayout
    }

    private fun deleteDepartment () {
        val sharedPref = getSharedPreferences("store_Layout", Context.MODE_PRIVATE)
        var editor = sharedPref.edit()
        var departmentCount = sharedPref.getInt("departmentCount", 0)

        val departmentToDeleteEditText: EditText = findViewById(R.id.store_layout_delete_department_editText)
        var orderNumberToDelete:Int = 0

        //if order number is not empty we will use whatever input value is in the field
        if (departmentToDeleteEditText.text.isNotEmpty())
            orderNumberToDelete = departmentToDeleteEditText.text.toString().toInt()

        if(orderNumberToDelete < 1 || orderNumberToDelete > departmentCount){
            Toast.makeText(this, "Entry not found in list", Toast.LENGTH_SHORT).show()
        }else{
            for(i in orderNumberToDelete..(departmentCount-1)){
                editor.putString(i.toString(), sharedPref.getString((i+1).toString(), ""))
            }
            editor.remove(departmentCount.toString())
            departmentCount = departmentCount - 1
            editor.putInt("departmentCount", departmentCount)
            editor.apply()
            updateStoreLayoutView()
        }

        departmentToDeleteEditText.text.clear()
    }
}
