package com.example.simplist2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myStaplesButton: Button = findViewById(R.id.my_staples_button)
        myStaplesButton.setOnClickListener {
            val intent = Intent(this, MyStaplesActivity::class.java)
            startActivity(intent)
        }

        val storeLayoutButton: Button = findViewById(R.id.store_layout_button)
        storeLayoutButton.setOnClickListener {
            val intent = Intent(this, StoreLayoutActivity::class.java)
            startActivity(intent)
        }

        val shoppingListButton: Button = findViewById(R.id.shopping_list_button)
        shoppingListButton.setOnClickListener {
            val intent = Intent(this, ShoppingListActivity::class.java)
            startActivity(intent)
        }
    }
}