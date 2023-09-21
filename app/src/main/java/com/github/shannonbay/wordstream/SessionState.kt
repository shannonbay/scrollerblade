package com.github.shannonbay.wordstream

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.preference.PreferenceManager
import java.util.UUID
import kotlin.reflect.KProperty

private lateinit var root : UUID
private lateinit var context: Context

fun initSessionState(applicationContext: Context) {

    context = applicationContext
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    initSessionStateRoot(sharedPreferences)
}


fun initSessionStateRoot(sp: SharedPreferences) {
    root = UUID.fromString(sp.getString("ROOT", UUID.randomUUID().toString()))
    sp.edit().putString("ROOT", root.toString())
    sp.edit().apply()
}

abstract class SessionStateField<T>(val row: UUID, val name: String, var _value: T, val context: Context) : ViewModel() {
    override fun toString(): String {
        return value.toString()
    }

    internal val default = _value
    internal val sharedPreferences : SharedPreferences by lazy {
        context.getSharedPreferences(
            row.toString(),
            AppCompatActivity.MODE_PRIVATE
        )
    }
    internal val editor: SharedPreferences.Editor = sharedPreferences.edit()

    private val _valueLiveData = MutableLiveData<T?>()

    val valueLiveData: LiveData<String> by lazy {
        _valueLiveData.map { a ->
            Log.d("STATE", "Transformed ${a}")
            a.toString()
        }
    }
//        get() = _valueLiveData

    open var value: T
        get() {
            if(isDirty) {
                _value = getDirty()
                isDirty = false
                return _value
            }
            return _value
        }
        set(newValue) {
            if (_value != newValue) {
                _value = newValue
                // Set the dirty flag when the value changes
                isDirty = true
                Log.e("STATE", "Posting $newValue")
                _valueLiveData.postValue( newValue )
                _valueLiveData.setValue( newValue )
                _valueLiveData.value = newValue
            }
        }

    var isDirty: Boolean = true
        private set
    internal abstract fun getDirty(): T
}


private operator fun ObservableInt.inc(): ObservableInt {
    this.set(this.get()+1)
    return this
}

class RefField(row: UUID, name: String, value: UUID, context: Context) : SessionStateField<UUID>(row, name, value, context) {
    fun apply(){
        editor.putString(name, value.toString())
        editor.apply()
    }

    override fun getDirty(): UUID {
        return UUID.fromString(sharedPreferences.getString(name, default.toString()))
    }
}

class StringField(row: UUID, name: String, value: String, context: Context) : SessionStateField<String>(row, name, value, context) {
    fun apply(){
        editor.putString(name, value)
        editor.apply()
    }

    override fun getDirty(): String {
        return sharedPreferences.getString(name, default)!!
    }
}

class IntField(row: UUID, name: String, value: Int, context: Context) : SessionStateField<Int>(row, name, value, context) {
    override var value: Int
       get() {
            return super.value
        }
        set(newValue) {
            super.value = newValue
        }

    operator fun inc(): IntField {
        _value++
        return this
    }
    fun apply(){
        if(isDirty) {
            editor.putInt(name, value)
            editor.apply()
        }
    }

    override fun getDirty(): Int {
        return sharedPreferences.getInt(name, default)
    }
}

/**
 * @param value default if no value already exists, otherwise it is initial value and default
 */
fun createIntField(name: String, value: Int): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(IntField::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return IntField(root, name, value, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

class LazyInitializer<T>(private val initializer: () -> T) {
    private var initializedValue: T = initializer()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return initializedValue
    }
}