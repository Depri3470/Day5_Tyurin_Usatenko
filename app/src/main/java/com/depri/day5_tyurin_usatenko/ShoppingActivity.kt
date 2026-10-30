package com.depri.day5_tyurin_usatenko

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class ShoppingActivity : AppCompatActivity() {

    // Список данных и адаптер
    val shoppingList = mutableListOf<String>()
    lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shopping)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.shoppingLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Светлый фон экрана — тёмные иконки статус-бара
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        val etItem = findViewById<EditText>(R.id.etItem)
        val btnAdd = findViewById<Button>(R.id.btnItemAdd)
        val btnClearAll = findViewById<Button>(R.id.btnItemClearAll)
        val lvShopping = findViewById<ListView>(R.id.lvShopping)

        // Привязываем ArrayAdapter к нашему списку
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, shoppingList)
        lvShopping.adapter = adapter

        // Восстанавливаем данные при старте
        loadShoppingList()

        // Добавление новой покупки
        btnAdd.setOnClickListener {
            val text = etItem.text.toString().trim()
            if (text.isNotEmpty()) {
                shoppingList.add(text)
                saveShoppingList()
                adapter.notifyDataSetChanged()
                etItem.text.clear()
            } else {
                Toast.makeText(this, "Введите название товара", Toast.LENGTH_SHORT).show()
            }
        }

        // Удаление товара по клику на элемент списка
        lvShopping.setOnItemClickListener { _, _, position, _ ->
            val removed = shoppingList.removeAt(position)
            saveShoppingList()
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Удалено: $removed", Toast.LENGTH_SHORT).show()
        }

        // Полная очистка
        btnClearAll.setOnClickListener {
            shoppingList.clear()
            val sharedPreferences = getSharedPreferences("ShoppingPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Список полностью очищен", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveShoppingList() {
        val sharedPreferences = getSharedPreferences("ShoppingPrefs", Context.MODE_PRIVATE)
        val serialized = shoppingList.joinToString(";;;")
        sharedPreferences.edit().putString("saved_items", serialized).apply()
    }

    fun loadShoppingList() {
        val sharedPreferences = getSharedPreferences("ShoppingPrefs", Context.MODE_PRIVATE)
        val rawString = sharedPreferences.getString("saved_items", "") ?: ""
        shoppingList.clear()
        if (rawString.isNotEmpty()) {
            shoppingList.addAll(rawString.split(";;;").filter { it.isNotBlank() })
        }
        adapter.notifyDataSetChanged()
    }
}
