package jp.techacademy.chiharu.akiba.taskapp

import android.app.Application
import io.realm.Realm

class TaskApp: Application(){
    override fun onCreate(){
        super.onCreate()
        Realm.init(this)
    }
}