package com.example.simplist2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*

class ShoppingListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        //load shopping list
        updateShoppingList()

        //establish autocomplete for department
        val depEntry: AutoCompleteTextView = findViewById(R.id.shopping_list_dep_name_editText)

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

        val addItemButton: Button = findViewById(R.id.shopping_list_add_button)
        addItemButton.setOnClickListener {
            addItemToShoppingList()
        }

        val doneButton: Button = findViewById(R.id.shopping_list_done_button)
        doneButton.setOnClickListener {
            clearShoppingList()
            //return to main menu
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun updateShoppingList(){
        val sharedPrefQty = getSharedPreferences("itemsQty", Context.MODE_PRIVATE)
        val sharedPrefShopList = getSharedPreferences("shopList", Context.MODE_PRIVATE)
        val sharedPrefDep = getSharedPreferences("itemsDepartment", Context.MODE_PRIVATE)
        val sharedPrefLayout = getSharedPreferences("store_Layout", Context.MODE_PRIVATE)

        //Sort by Department
        var departmentCount = sharedPrefLayout.getInt("departmentCount", 0)
        val itemDepartments: Map<String, String> = sharedPrefDep.all as Map<String, String>
        //val departments: MutableList<String> = mutableListOf()

        //specify outer layout
        val outerLayout = findViewById(R.id.shoppingListLinearLayout) as LinearLayout
        outerLayout.removeAllViews()

        for (i in 1..departmentCount){
                //create department heading
                val textView = TextView(this)
                textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                textView.text = sharedPrefLayout.getString(i.toString(), "")
                outerLayout.addView(textView)

                //add all items in that department
                for(j in itemDepartments){
                    if((j.value.toString() == sharedPrefLayout.getString(i.toString(), ""))
                            && (sharedPrefShopList.contains(j.key))){

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
                            //remove item from shopping list
                            sharedPrefShopList.edit().remove(j.key.toString()).apply()
                        }
                        rowLayout.addView(button)

                        //create item text view
                        val textView = TextView(this)
                        textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        textView.text = j.key
                        rowLayout.addView(textView)

                        //create qty text view
                        val qtyTextView = TextView(this)
                        qtyTextView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        qtyTextView.text = "  " + sharedPrefShopList.getInt(j.key, 0).toString() + "  "
                        rowLayout.addView(qtyTextView)

                        //create check off button
                        val checkOffButton = Button(this)
                        checkOffButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        checkOffButton.text = """✅"""
                        checkOffButton.setBackgroundColor(Color.WHITE)
                        checkOffButton.setOnClickListener {
                            //we will just remove the row, but not remove from shopping list
                            //that way, we can still reset the list. Items are only cleared from the
                            //shopping list preferences when they are deleted, or when the done button is pressed
                            outerLayout.removeView(rowLayout)
                        }
                        rowLayout.addView(checkOffButton)

                        //create save button
                        val repeatButton = Button(this)
                        repeatButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        repeatButton.text = """♻"""
                        repeatButton.setBackgroundColor(Color.WHITE)
                        repeatButton.setOnClickListener {
                            //add to item qty
                            val qty = sharedPrefShopList.getInt(j.key, 0)
                            sharedPrefQty.edit().putInt(j.key, qty).apply()
                            //delete from shopping list
                            sharedPrefShopList.edit().remove(j.key).apply()
                            //remove from view
                            outerLayout.removeView(rowLayout)
                        }
                        rowLayout.addView(repeatButton)
                    }
                }
        }
    }

    fun addItemToShoppingList(){
        //get item and department
        val shoppingItemEditText: EditText = findViewById(R.id.shopping_list_item_editText)
        val shoppingDepartmentEditText: EditText = findViewById(R.id.shopping_list_dep_name_editText)
        val item = shoppingItemEditText.text.toString()
        val departmentName: String = shoppingDepartmentEditText.text.toString()

        val sharedPrefDep = getSharedPreferences("itemsDepartment", Context.MODE_PRIVATE)
        val sharedPrefStoreLay = getSharedPreferences("store_Layout", Context.MODE_PRIVATE)
        val sharedPrefShopList = getSharedPreferences("shopList", Context.MODE_PRIVATE)

        //get list of valid departments (must be added to store layout to be valid)
        val storeLay: Map<String, *> = sharedPrefStoreLay.all
        val departmentList: MutableList<String> = mutableListOf()
        for (i in storeLay){
            departmentList.add(i.value.toString())
        }

        //check if item already exists, only add if it doesn't already exist

        if(sharedPrefShopList.contains(item))
            Toast.makeText(this, "$item already exists", Toast.LENGTH_SHORT).show()
        else {
            //add item to the correct department, once verified it is a valid department name
            //I check department name before adding the item because we dont want to add an item
            //until it has a valid deparment name associated with it.
            if(departmentList.contains(departmentName)) {

                //add item to list and set quantity to 0
                sharedPrefShopList.edit().putInt(item, 1).apply()

                //add item to the department

                sharedPrefDep.edit().putString(item, departmentName).apply()

                //add item xml to the screen
                updateShoppingList()
            }
            else
                Toast.makeText(this, "Department not valid", Toast.LENGTH_SHORT).show()
        }

        //clear input fields

        shoppingItemEditText.text.clear()
        shoppingDepartmentEditText.text.clear()
    }

    fun clearShoppingList(){
        //clear all shopping list preferences
        val sharedPrefShopList = getSharedPreferences("shopList", Context.MODE_PRIVATE)
        sharedPrefShopList.edit().clear().commit()
    }
}

