package com.depri.day5_tyurin_usatenko

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private val dishes = listOf(
        "Пицца Маргарита", "Борщ", "Цезарь с курицей",
        "Паста Карбонара", "Чизкейк", "Том Ям"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализируем элементы интерфейса
        val etName = findViewById<EditText>(R.id.etName)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnLoad = findViewById<Button>(R.id.btnLoad)
        val btnClear = findViewById<Button>(R.id.btnClear)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        // Открываем хранилище "MyPrefs" в приватном режиме
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // ===== Задание 1: сохранение имени =====

        // Сохранение с валидацией ввода
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите имя!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val editor = sharedPreferences.edit()
            editor.putString("user_name", name)
            editor.apply() // Сохраняем асинхронно
            Toast.makeText(this, "Имя успешно сохранено!", Toast.LENGTH_SHORT).show()
            etName.text.clear()
        }

        // Загрузка сохранённого значения
        btnLoad.setOnClickListener {
            val savedName = sharedPreferences.getString("user_name", null)
            if (savedName != null) {
                tvResult.text = "Сохранённое имя: $savedName"
                Toast.makeText(this, "Загружено: $savedName", Toast.LENGTH_SHORT).show()
            } else {
                tvResult.text = "Имя ещё не сохранено в памяти"
                Toast.makeText(this, "Данные не найдены", Toast.LENGTH_SHORT).show()
            }
        }

        // Удаление конкретного ключа
        btnClear.setOnClickListener {
            if (sharedPreferences.contains("user_name")) {
                sharedPreferences.edit().remove("user_name").apply()
                tvResult.text = "Данные удалены"
                Toast.makeText(this, "Ключ 'user_name' удалён", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Нечего удалять", Toast.LENGTH_SHORT).show()
            }
        }

        // ===== Задание 2: тёмная / светлая тема =====
        val switchDarkMode = findViewById<SwitchCompat>(R.id.switchDarkMode)

        // Считываем сохранённую настройку (по умолчанию false - светлая)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        switchDarkMode.isChecked = isDarkMode
        applyCustomTheme(isDarkMode)

        // Обрабатываем переключение тумблера пользователем
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPreferences.edit()
            editor.putBoolean("dark_mode", isChecked)
            editor.apply()
            applyCustomTheme(isChecked)
        }

        // ===== Задание 3: счётчик запусков и первый вход =====
        val tvLaunchCount = findViewById<TextView>(R.id.tvLaunchCount)
        val tvFirstRunStatus = findViewById<TextView>(R.id.tvFirstRunStatus)

        // 1. Проверка первого запуска
        val isFirstRun = sharedPreferences.getBoolean("is_first_run", true)
        if (isFirstRun) {
            tvFirstRunStatus.text = "Статус: Приложение запущено впервые! Добро пожаловать!"
            // Отмечаем, что первый запуск уже состоялся
            sharedPreferences.edit().putBoolean("is_first_run", false).apply()
        } else {
            tvFirstRunStatus.text = "Статус: Повторный запуск постоянного пользователя"
        }

        // 2. Инкремент счётчика сессий (только при новом запуске, а не при повороте экрана)
        var launchCount = sharedPreferences.getInt("launch_count", 0)
        if (savedInstanceState == null) {
            launchCount++
            sharedPreferences.edit()
                .putInt("launch_count", launchCount)
                .apply()
        }
        tvLaunchCount.text = "Вы открыли это приложение $launchCount раз(а)"

        // ===== Задание 4: избранные блюда =====
        val spDishes = findViewById<Spinner>(R.id.spDishes)
        val btnFavorite = findViewById<Button>(R.id.btnFavorite)
        val btnShowFavorites = findViewById<Button>(R.id.btnShowFavorites)
        val tvFavorites = findViewById<TextView>(R.id.tvFavorites)

        spDishes.adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dishes) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(if (switchDarkMode.isChecked) Color.WHITE else Color.BLACK)
                return view
            }
        }

        btnFavorite.setOnClickListener {
            addDishToFavorites(spDishes.selectedItem.toString())
        }

        btnShowFavorites.setOnClickListener {
            val favorites = loadFavoritesList()
            tvFavorites.text = if (favorites.isEmpty()) {
                "Избранное пусто"
            } else {
                "Избранное:\n" + favorites.joinToString("\n") { "★ $it" }
            }
        }

        // ===== Задание 5: список покупок =====
        findViewById<Button>(R.id.btnOpenShopping).setOnClickListener {
            startActivity(Intent(this, ShoppingActivity::class.java))
        }
    }

    // Функция смены цвета фона и текста
    private fun applyCustomTheme(isDark: Boolean) {
        val mainLayout = findViewById<ConstraintLayout>(R.id.mainLayout)
        val textViews = listOf(
            R.id.tvTitle, R.id.tvMenuTitle, R.id.tvLaunchCount,
            R.id.tvFirstRunStatus, R.id.tvFavorites
        ).map { findViewById<TextView>(it) }
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val switchDarkMode = findViewById<SwitchCompat>(R.id.switchDarkMode)
        val etName = findViewById<EditText>(R.id.etName)
        val spDishes = findViewById<Spinner>(R.id.spDishes)
        (spDishes.selectedView as? TextView)?.setTextColor(if (isDark) Color.WHITE else Color.BLACK)
        // Иконки статус-бара: тёмные на светлом фоне, светлые на тёмном
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = !isDark

        if (isDark) {
            mainLayout.setBackgroundColor(Color.parseColor("#212121"))
            textViews.forEach { it.setTextColor(Color.WHITE) }
            tvResult.setTextColor(Color.LTGRAY)
            switchDarkMode.setTextColor(Color.WHITE)
            etName.setTextColor(Color.WHITE)
            etName.setHintTextColor(Color.GRAY)
        } else {
            mainLayout.setBackgroundColor(Color.WHITE)
            textViews.forEach { it.setTextColor(Color.BLACK) }
            tvResult.setTextColor(Color.DKGRAY)
            switchDarkMode.setTextColor(Color.BLACK)
            etName.setTextColor(Color.BLACK)
            etName.setHintTextColor(Color.GRAY)
        }
    }

    // Добавление блюда в Избранное
    private fun addDishToFavorites(dishName: String) {
        // Получаем текущее множество или пустое
        val currentFavorites = sharedPreferences.getStringSet("favorites_set", emptySet()) ?: emptySet()

        // ВАЖНО: обязательно создаём НОВУЮ копию коллекции HashSet!
        val updatedFavorites = HashSet<String>(currentFavorites)

        if (updatedFavorites.contains(dishName)) {
            Toast.makeText(this, "$dishName уже есть в избранном!", Toast.LENGTH_SHORT).show()
        } else {
            updatedFavorites.add(dishName)
            // Сохраняем обновлённую копию
            sharedPreferences.edit()
                .putStringSet("favorites_set", updatedFavorites)
                .apply()
            Toast.makeText(this, "$dishName добавлено в избранное!", Toast.LENGTH_SHORT).show()
        }
    }

    // Загрузка и отображение списка избранного
    private fun loadFavoritesList(): List<String> {
        val favoritesSet = sharedPreferences.getStringSet("favorites_set", emptySet()) ?: emptySet()
        return favoritesSet.toList()
    }
}
