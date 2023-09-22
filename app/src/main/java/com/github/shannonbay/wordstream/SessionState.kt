package com.github.shannonbay.wordstream

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import java.util.UUID
import kotlin.reflect.KProperty

private lateinit var root : UUID
private lateinit var context: Context

fun initSessionState(applicationContext: Context) {

    context = applicationContext
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    sharedPreferences.all

    initSessionStateRoot(sharedPreferences)
}


fun initSessionStateRoot(sp: SharedPreferences) {
    root = UUID.fromString(sp.getString("ROOT", UUID.randomUUID().toString()))
    Log.d("STATE", "UUID ROOT: $root")
    val editor = sp.edit();
    editor.putString("ROOT", root.toString())
    editor.apply()
    editor.commit()
}

class LazyInit<T>(private val initializer: () -> T) {
    private var _blah : T = initializer()
    var blah: T
        get() {
            return _blah
        }
        set(newValue) {
            _blah = newValue
        }
}

data class Key(val row: UUID, val name: String)

abstract class SessionStateField<T>(val row: UUID, val name: String, val default: T, val context: Context) {
    override fun toString(): String {
        return "{ $name $value }"
    }

    internal val _value by lazy { LazyInit { getDirty() } }

    //    protected var _value = default
    internal val sharedPreferences : SharedPreferences by lazy {
        context.getSharedPreferences(
            row.toString(),
            AppCompatActivity.MODE_PRIVATE
        )
    }
    internal val editor: SharedPreferences.Editor = sharedPreferences.edit()

    private val _valueLiveData = MutableLiveData<T?>()
    val valueLiveData : MutableLiveData<T?>
        get() {
            Log.d("STATE", "Sending stuff to map")
            return _valueLiveData
        }
//        get() = _valueLiveData

    var value: T
        get() {
            return _value.blah
        }
        set(newValue) {
            if (_value != newValue) {
                _value.blah = newValue
                // Set the dirty flag when the value changes
                isDirty = true
                Log.e("STATE", "Posting $newValue $_value $value ")
                valueLiveData.postValue( newValue )
                valueLiveData.setValue( newValue )
                valueLiveData.value = newValue
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

class StringSetField(row: UUID, name: String, val __value: Set<String>, context: Context) : SessionStateField<Set<String>>(row, name, __value, context) {
    fun apply(){
        editor.putStringSet(name, value)
        editor.apply()
    }

    override fun getDirty(): Set<String> {
        return sharedPreferences.getStringSet(name, default)!!
    }
}


class StringField(row: UUID, name: String, val __value: String, context: Context) : SessionStateField<String>(row, name, __value, context) {
    fun apply(){
        editor.putString(name, value)
        editor.apply()
    }

    override fun getDirty(): String {
        return sharedPreferences.getString(name, default)!!
    }
}

class IntField(row: UUID, name: String, value: Int, context: Context) : SessionStateField<Int>(row, name, value, context) {
    operator fun inc(): IntField {
        value.inc()
        return this
    }
    fun apply(){
        if(isDirty) {
            Log.d("STATE", "Committing $name $value")
            editor.putInt(name, value)
            editor.apply()
            editor.commit()
        }
    }

    override fun getDirty(): Int {
        return sharedPreferences.getInt(name, default)
    }
}

/**
 * @param value default if no value already exists, otherwise it is initial value and default
 */
fun createIntField(name: String, value: Int): IntField {
   return IntField(root, name, value, context)
}

fun createStringField(name: String, value: String): StringField {
       return StringField(root, name, value, context)
}

fun createStringSetField(name: String, value: Set<String>): StringSetField {
    return StringSetField(root, name, value, context)
}

class LazyInitializer<T>(private val initializer: () -> T) {
    private var initializedValue: T = initializer()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return initializedValue
    }
}