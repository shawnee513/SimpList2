package com.example.simplist2

import android.R.interpolator.linear
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginStart


class MyStaplesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_staples)

        updateMyStaplesLayoutView()

        //establish autocomplete for department
        val depEntry: AutoCompleteTextView = findViewById(R.id.my_staples_department_editText)

        //establish list of departments
        val sharedPrefStoreLay = getSharedPreferences("store_Layout", Context.MODE_PRIVATE)
        var count = sharedPrefStoreLay.getInt("departmentCount", 0)
        val departments: Array<String> = Array<String>(count){""}
        val storeLay: Map<String, *> = sharedPrefStoreLay.all
        var index = 0
        for (i in storeLay){
            if(index < count && (i.key != "departmentCount")) {
                departments[index] = i.value.toString()
                index += 1
            }
        }
        //set adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_selectable_list_item, departments)
        depEntry.setAdapter(adapter)

        val addItemButton: Button = findViewById(R.id.my_staples_add_button)
        addItemButton.setOnClickListener {
            addItemToStaples()
        }

        val shopButton: Button = findViewById(R.id.my_staples_shop_button)
        shopButton.setOnClickListener {
            //add staple items to shopping list
            addStaples()

            //start shopping activity
            val intent = Intent(this, ShoppingListActivity::class.java)
            startActivity(intent)

            //updateMyStaplesLayoutView()
        }

        var deleteToggle = false;

        val deleteButton: Button = findViewById(R.id.my_staples_delete_button)
        deleteButton.setOnClickListener {
            if (deleteToggle == false){
                layoutWithDeleteButton()
                deleteToggle = true
            }
            else {
                updateMyStaplesLayoutView()
                deleteToggle = false
            }
        }
    }

    private fun addItemToStaples () {
        //get item and department
        val staplesItemEditText: EditText = findViewById(R.id.my_staples_item_editText)
        val staplesDepartmentEditText: AutoCompleteTextView = findViewById(R.id.my_staples_department_editText)
        val staplesLocationEditText: EditText = findViewById(R.id.my_staples_location_editText)
        val item = staplesItemEditText.text.toString()
        val departmentName: String = staplesDepartmentEditText.text.toString()
        var location = staplesLocationEditText.text.toString()

        //if no location was specified, set location to default "no location specified"
        if (location == "")
            location = "No Location Specified"

        val sharedPrefQty = getSharedPreferences("itemsQty", Context.MODE_PRIVATE)
        val sharedPrefDep = getSharedPreferences("itemsDepartment", Context.MODE_PRIVATE)
        val sharedPrefLoc = getSharedPreferences("itemsLocation", Context.MODE_PRIVATE)
        val sharedPrefStoreLay = getSharedPreferences("store_Layout", Context.MODE_PRIVATE)

        //get list of valid departments (must be added to store layout to be valid)

        val storeLay: Map<String, *> = sharedPrefStoreLay.all
        val departmentList: MutableList<String> = mutableListOf()
        for (i in storeLay){
            departmentList.add(i.value.toString())
        }

        //check if item already exists, only add if it doesn't already exist

        if(sharedPrefQty.contains(item))
            Toast.makeText(this, "$item already exists", Toast.LENGTH_SHORT).show()
        else {

            //add item to the correct department, once verified it is a valid department name
            //I check department name before adding the item because we dont want to add an item
            //until it has a valid deparment name associated with it.
            if(departmentList.contains(departmentName)) {

                //add item to list and set quantity to 0
                sharedPrefQty.edit().putInt(item, 0).apply()

                //add item to the department

                sharedPrefDep.edit().putString(item, departmentName).apply()

                //add item to the location

                sharedPrefLoc.edit().putString(item, location).apply()

                //add item xml to the screen
                updateMyStaplesLayoutView()
            }
            else
                Toast.makeText(this, "Department not valid", Toast.LENGTH_SHORT).show()
        }

        //clear input fields

        staplesItemEditText.text.clear()
        staplesDepartmentEditText.text.clear()
        staplesLocationEditText.text.clear()
    }

    fun addStaples(){
        val sharedPrefQty = getSharedPreferences("itemsQty", Context.MODE_PRIVATE)
        val sharedPrefShopList = getSharedPreferences("shopList", Context.MODE_PRIVATE)

        //get all items from itemsQty and add them to shopList
        val items: Map<String, Int> = sharedPrefQty.all as Map<String, Int>

        //now iterate through items and if quantity is greater than zero we will add the item to the shop list
        for (item in items){
            if (item.value > 0){
                var qty = sharedPrefShopList.getInt(item.key, 0)
                qty += item.value
                sharedPrefShopList.edit().putInt(item.key, qty).apply()
                //set qty back to zero in staples
                sharedPrefQty.edit().putInt(item.key, 0).apply()
            }
        }
        //updateMyStaplesLayoutView()
    }

    fun updateMyStaplesLayoutView(){
        val sharedPrefQty = getSharedPreferences("itemsQty", Context.MODE_PRIVATE)
        val sharedPrefLoc = getSharedPreferences("itemsLocation", Context.MODE_PRIVATE)
        val sharedPrefDep = getSharedPreferences("itemsDepartment", Context.MODE_PRIVATE)

        //get list of locations so we can sort items by location
        val itemLocations: Map<String, String> = sharedPrefLoc.all as Map<String, String>
        val locations: MutableList<String> = mutableListOf()



        /*val storeLay: Map<String, > = sharedPrefStoreLay.all
        val departmentList: MutableList<String> = mutableListOf()
        for (i in storeLay){
            departmentList.add(i.value.toString())
        }*/

        //create linear layout - horizontal
        val outerLayout = findViewById(R.id.linearLayoutTest) as LinearLayout
        outerLayout.removeAllViews()

        for (i in itemLocations){
            if (!(locations.contains(i.value.toString()))){

                //add location to location list so it doesn't get repeated
                locations.add(i.value.toString())

                //create location heading
                val textView = TextView(this)
                textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                textView.setTextColor(0xFFFFFFFF.toInt())
                textView.setBackgroundColor(0xFF03DAC5.toInt())
                textView.text = i.value.toString()
                outerLayout.addView(textView)

                //add all items with that location
                for(j in itemLocations){
                    if(j.value.toString() == i.value.toString()){

                        //create the row where the elements for each item will reside

                        var rowLayout = LinearLayout(this)
                        rowLayout.orientation = LinearLayout.HORIZONTAL
                        rowLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        outerLayout.addView(rowLayout)

                        //now add elements to the row

                        //create item text view
                        val textView = TextView(this)
                        textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        textView.text = j.key + "  "
                        rowLayout.addView(textView)

                        //create quantity textView
                        val qtytextView = TextView(this)
                        qtytextView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        qtytextView.text = "  " + sharedPrefQty.getInt(j.key, 0).toString() + "  "

                        //create decrease button
                        val decreaseButton = Button(this)
                        decreaseButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        decreaseButton.text = "-"
                        decreaseButton.setBackgroundColor(Color.WHITE)
                        decreaseButton.setOnClickListener {
                            var qty = sharedPrefQty.getInt(j.key, 0)
                            qty -= 1
                            if (qty < 0)
                                qty = 0
                            sharedPrefQty.edit().putInt(j.key, qty).apply()
                            qtytextView.text = qty.toString()
                        }
                        rowLayout.addView(decreaseButton)
                        rowLayout.addView(qtytextView)

                        //create increase button
                        val increaseButton = Button(this)
                        increaseButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        increaseButton.text = "+"
                        increaseButton.setBackgroundColor(Color.WHITE)
                        increaseButton.setOnClickListener {
                            var qty = sharedPrefQty.getInt(j.key, 0)
                            qty += 1
                            sharedPrefQty.edit().putInt(j.key, qty).apply()
                            qtytextView.text = qty.toString()
                        }
                        rowLayout.addView(increaseButton)
                    }
                }
            }
        }

        //establish autocomplete for location
        val locEntry: AutoCompleteTextView = findViewById(R.id.my_staples_location_editText)

        //establish list of locations
        val locCount = locations.size
        val locationArray: Array<String> = Array<String>(locCount){""}
        var index = 0
        for (i in locations){
            if(index < locCount) {
                locationArray[index] = i.toString()
                index += 1
            }
        }
        //set adapter
        val locAdapter = ArrayAdapter(this, android.R.layout.simple_selectable_list_item, locationArray)
        locEntry.setAdapter(locAdapter)

    }

    fun layoutWithDeleteButton(){
        val sharedPrefQty = getSharedPreferences("itemsQty", Context.MODE_PRIVATE)
        val sharedPrefLoc = getSharedPreferences("itemsLocation", Context.MODE_PRIVATE)
        val sharedPrefDep = getSharedPreferences("itemsDepartment", Context.MODE_PRIVATE)

        //get list of locations so we can sort items by location
        val itemLocations: Map<String, String> = sharedPrefLoc.all as Map<String, String>
        val locations: MutableList<String> = mutableListOf()



        /*val storeLay: Map<String, > = sharedPrefStoreLay.all
        val departmentList: MutableList<String> = mutableListOf()
        for (i in storeLay){
            departmentList.add(i.value.toString())
        }*/

        //create linear layout - horizontal
        val outerLayout = findViewById(R.id.linearLayoutTest) as LinearLayout
        outerLayout.removeAllViews()

        for (i in itemLocations){
            if (!(locations.contains(i.value.toString()))){

                //add location to location list so it doesn't get repeated
                locations.add(i.value.toString())

                //create location heading
                val textView = TextView(this)
                textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                textView.setTextColor(0xFFFFFFFF.toInt())
                textView.setBackgroundColor(0xFF03DAC5.toInt())
                textView.text = i.value.toString()
                outerLayout.addView(textView)

                //add all items with that location
                for(j in itemLocations){
                    if(j.value.toString() == i.value.toString()){

                        //create the row where the elements for each item will reside

                        var rowLayout = LinearLayout(this)
                        rowLayout.orientation = LinearLayout.HORIZONTAL
                        rowLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        outerLayout.addView(rowLayout)

                        //now add elements to the row

                        //create delete button
                        val button = Button(this)
                        button.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        button.text = """☒"""
                        button.setBackgroundColor(Color.WHITE)
                        button.setOnClickListener {
                            outerLayout.removeView(rowLayout)
                            //remove item from qty list
                            sharedPrefQty.edit().remove(j.key.toString()).apply()
                            //remove item from location list
                            sharedPrefLoc.edit().remove(j.key.toString()).apply()
                            //remove item from department list
                            sharedPrefDep.edit().remove(j.key.toString()).apply()
                        }
                        rowLayout.addView(button)

                        //create item text view
                        val textView = TextView(this)
                        textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        textView.text = j.key + "  "
                        rowLayout.addView(textView)

                        //create quantity textView
                        val qtytextView = TextView(this)
                        qtytextView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        qtytextView.text = "  " + sharedPrefQty.getInt(j.key, 0).toString() + "  "

                        //create decrease button
                        val decreaseButton = Button(this)
                        decreaseButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        decreaseButton.text = "-"
                        decreaseButton.setBackgroundColor(Color.WHITE)
                        decreaseButton.setOnClickListener {
                            var qty = sharedPrefQty.getInt(j.key, 0)
                            qty -= 1
                            if (qty < 0)
                                qty = 0
                            sharedPrefQty.edit().putInt(j.key, qty).apply()
                            qtytextView.text = qty.toString()
                        }
                        rowLayout.addView(decreaseButton)
                        rowLayout.addView(qtytextView)

                        //create increase button
                        val increaseButton = Button(this)
                        increaseButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        increaseButton.text = "+"
                        increaseButton.setBackgroundColor(Color.WHITE)
                        increaseButton.setOnClickListener {
                            var qty = sharedPrefQty.getInt(j.key, 0)
                            qty += 1
                            sharedPrefQty.edit().putInt(j.key, qty).apply()
                            qtytextView.text = qty.toString()
                        }
                        rowLayout.addView(increaseButton)
                    }
                }
            }
        }

        //establish autocomplete for location
        val locEntry: AutoCompleteTextView = findViewById(R.id.my_staples_location_editText)

        //establish list of locations
        val locCount = locations.size
        val locationArray: Array<String> = Array<String>(locCount){""}
        var index = 0
        for (i in locations){
            if(index < locCount) {
                locationArray[index] = i.toString()
                index += 1
            }
        }
        //set adapter
        val locAdapter = ArrayAdapter(this, android.R.layout.simple_selectable_list_item, locationArray)
        locEntry.setAdapter(locAdapter)

    }
}

