package jp.techacademy.chiharu.akiba.taskapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.RealmChangeListener
import io.realm.Sort
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import android.app.AlarmManager
import android.app.PendingIntent
import io.realm.RealmResults
import kotlinx.android.synthetic.main.content_input.*

const val EXTRA_TASK = "jp.techacademy.chiharu.akiba.taskapp.TASK"

class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // AppCompatActivityのメソッドを呼んでいる
        setContentView(R.layout.activity_main) //　xmlファイルをuiにして画面に描く

        fab.setOnClickListener { view ->
            val intent = Intent(this, InputActivity::class.java) // InputActivityを呼び出すためのIntentクラスのインスタンスを作成
            startActivity(intent) // Intentのインスタンスを使ってInputActivityを呼び出す
        } // ボタンにリスナーを付ける

        search_button.setOnClickListener { view ->
            reloadListView()
        } // ボタンにリスナーを付ける

        // Realmの設定
        mRealm = Realm.getDefaultInstance() // Realm（データベースソフトの名前）を使うためにインスタンスを取得する
        mRealm.addChangeListener(mRealmListener)

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this)

        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK") { _, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)


                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }
        reloadListView()
    }

    private fun reloadListView() {
        val category = search_edit_text.text.toString()
        val taskRealmResults : RealmResults<Task>
        if (category == "") {
            // Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得
            taskRealmResults =
                mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)
        }
        else {
            taskRealmResults =
                mRealm.where(Task::class.java).equalTo("category", category).findAll().sort("date", Sort.DESCENDING)
        }
        // 上記の結果を、TaskListとしてセットする
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }
}

//import android.os.Bundle
//import com.google.android.material.snackbar.Snackbar
//import androidx.appcompat.app.AppCompatActivity
//import io.realm.Realm
//import kotlinx.android.synthetic.main.activity_main.*
//import io.realm.RealmChangeListener
//import io.realm.Sort
//import java.util.*
//
//class MainActivity : AppCompatActivity() {
//    private lateinit var mRealm: Realm
//    private val mRealmListener = object : RealmChangeListener<Realm> {
//        override fun onChange(element: Realm) {
//            reloadListView()
//        }
//    }
//
//    private lateinit var mTaskAdapter:TaskAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }
//        mRealm = Realm.getDefaultInstance()
//        mRealm.addChangeListener(mRealmListener)
//
//        mTaskAdapter = TaskAdapter(this)
//
//        listView1.setOnItemClickListener{parent,view,position,id ->
//
//        }
//
//        listView1.setOnItemLongClickListener { parent, view, position, id ->
//            true
//        }
//        addTaskForTest()
//
//        reloadListView()
//    }
//    private fun reloadListView(){
//
//        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)
//
//        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)
//
//        listView1.adapter = mTaskAdapter
//
//        mTaskAdapter.notifyDataSetChanged()
//    }
//    override fun onDestroy() {
//        super.onDestroy()
//
//        mRealm.close()
//    }
//
//    private fun addTaskForTest() {
//        val task = Task()
//        task.title = "作業"
//        task.contents = "プログラムを書いてPUSHする"
//        task.date = Date()
//        task.id = 0
//        mRealm.beginTransaction()
//        mRealm.copyToRealmOrUpdate(task)
//        mRealm.commitTransaction()
//    }
//}
//import android.os.Bundle
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.google.android.material.snackbar.Snackbar
//import androidx.appcompat.app.AppCompatActivity
//import android.view.Menu
//import android.view.MenuItem
//
//class MainActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        setSupportActionBar(findViewById(R.id.toolbar))
//
//        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//}