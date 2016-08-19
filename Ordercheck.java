package s14003.std.it_college.ac.jp.pbl2016.OrderCheck;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import s14003.std.it_college.ac.jp.pbl2016.R;

public class Ordercheck extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private Database myHelper;
    private Handler mHandler;
    private ArrayAdapter<String> adapter;

    @Override
    public void onClick(View view) {

        String msg = "";
        int priceSum = 0;


        // 1. SQLiteDatabaseオブジェクトを取得
        SQLiteDatabase db = myHelper.getReadableDatabase();

        // 2. query()を呼び、検索を行う
        Cursor cursor =
                db.query(Database.TABLE_NAME, null, null, null, null, null,
                        Database.Columns._ID + " ASC");

        // 3. 読込位置を先頭にする。falseの場合は結果0件
        if(!cursor.moveToFirst()){
            cursor.close();
            db.close();
            return;
        }

        // 4. 列のindex(位置)を取得する
        int _idIndex = cursor.getColumnIndex(Database.Columns._ID);
        int nameIndex = cursor.getColumnIndex(Database.Columns.NAME);
        int priceIndex = cursor.getColumnIndex(Database.Columns.PRICE);
        int quantityIndex = cursor.getColumnIndex(Database.Columns.NUM);

        // 5. 行を読み込む。
        itemList.removeAll(itemList);
        msg += "これらの商品を購入してもよろしいですか？\n\n";
        do {
            ProductItem item = new ProductItem();
            item._id = cursor.getInt(_idIndex);
            item.name = cursor.getString(nameIndex);
            item.price = cursor.getInt(priceIndex);
            item.num = cursor.getInt(quantityIndex);

            Log.d("selectProductList",
                    "_id = " + item._id + "\n" +
                            "name = " + item.name + "\n" +
                            "price = " + item.price + "\n" +
                            "stock = " + item.num);

            itemList.add(item);

            msg += item.name + "  　　" + item.num + "個 　　 " + item.price + "円\n";
            priceSum += item.price;

            // 読込位置を次の行に移動させる
            // 次の行が無い時はfalseを返すのでループを抜ける
        }while (cursor.moveToNext());

        // 6. Cursorを閉じる
        cursor.close();

        // 7. データベースを閉じる
        db.close();

        msg += "\n合計金額:  " + priceSum + "円";


        AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
        alertDlg.setMessage(msg);
        alertDlg.setTitle("確認");
        alertDlg.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // OKボタンクリック処理
                    }
                }
        );
        alertDlg.setNegativeButton(
                "キャンセル",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }
        );

        alertDlg.create().show();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    private class ProductItem {
        int _id;
        String name;
        int price;
        int num;
    }

    private List<ProductItem> itemList;
    private ItemAdapter Itemadapter;


    private void selectProductList(){

        // 1. SQLiteDatabaseオブジェクトを取得
        SQLiteDatabase db = myHelper.getReadableDatabase();

        // 2. query()を呼び、検索を行う
        Cursor cursor =
                db.query(Database.TABLE_NAME, null, null, null, null, null,
                        Database.Columns._ID + " ASC");

        // 3. 読込位置を先頭にする。falseの場合は結果0件
        if(!cursor.moveToFirst()){
            cursor.close();
            db.close();
            return;
        }

        // 4. 列のindex(位置)を取得する
        int _idIndex = cursor.getColumnIndex(Database.Columns._ID);
        int nameIndex = cursor.getColumnIndex(Database.Columns.NAME);
        int priceIndex = cursor.getColumnIndex(Database.Columns.PRICE);
        int quantityIndex = cursor.getColumnIndex(Database.Columns.NUM);

        // 5. 行を読み込む。
        itemList.removeAll(itemList);
        do {
            ProductItem item = new ProductItem();
            item._id = cursor.getInt(_idIndex);
            item.name = cursor.getString(nameIndex);
            item.price = cursor.getInt(priceIndex);
            item.num = cursor.getInt(quantityIndex);

            Log.d("selectProductList",
                    "_id = " + item._id + "\n" +
                            "name = " + item.name + "\n" +
                            "price = " + item.price + "\n" +
                            "stock = " + item.num);

            itemList.add(item);

            // 読込位置を次の行に移動させる
            // 次の行が無い時はfalseを返すのでループを抜ける
        }while (cursor.moveToNext());

        // 6. Cursorを閉じる
        cursor.close();

        // 7. データベースを閉じる
        db.close();

        //return itemList;
    }
    private Spinner productSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordercheck);

        myHelper = new Database(this);

        mHandler = new Handler();

//        initTable();


        itemList = new ArrayList<>();
//        adapter =
//                new ItemAdapter(getApplicationContext(), 0,
//                        itemList);
        ListView listView =
                (ListView)findViewById(R.id.listProducts);
        listView.setAdapter(Itemadapter);
        setProductData();


        (new Thread(new Runnable() {
            @Override
            public void run() {
                setProductData();

                //メインスレッドのメッセージキューにメッセージを登録します。
                mHandler.post(new Runnable (){
                    //run()の中の処理はメインスレッドで動作されます。
                    public void run(){
//                        adapter.notifyDataSetChanged();
                    }
                });
            }
        })).start();

        listView.setOnItemClickListener(this);

        Button btn_cancel = (Button)findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button btnBuy = (Button)findViewById(R.id.btnBuy);
        btnBuy.setOnClickListener(this);


    }

    //Toastで表示
    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void setProductData(){

//        ProductItem item = new ProductItem();
//        item.name = "赤鉛筆";
//        item.price = 500;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "青鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "黄鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "緑鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "紫鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "茶色鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "青鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "黄鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "緑鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "紫鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "茶色鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "青鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "黄鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "緑鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "紫鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "茶色鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "青鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "黄鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "緑鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "紫鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//        item = new ProductItem();
//        item.name = "茶色鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemList.add(item);
//
//        adapter.notifyDataSetChanged();

        selectProductList();

    }

    private class ItemAdapter extends ArrayAdapter<ProductItem> {
        private LayoutInflater inflater;

        public ItemAdapter(Context context, int resource,
                           List<ProductItem> objects) {
            super(context, resource, objects);
            inflater =
                    (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Log.d("ProductList", "getView");

            View view = inflater.inflate(R.layout.order_row, null, false);
            TextView nameView = (TextView)view.findViewById(R.id.name);
            TextView priceView = (TextView)view.findViewById(R.id.price);
//            TextView quantityView = (TextView)view.findViewById(R.id.productspinner);
            adapter = new ArrayAdapter<>(Ordercheck.this, android.R.layout.simple_spinner_item);

            for (int i = 1; i <= 100/*ここに数量を入れる*/; i++) {
                adapter.add(String.valueOf(i));
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            productSpinner = (Spinner)view.findViewById(R.id.productspinner);
            productSpinner.setAdapter(adapter);
            productSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Spinner spinner = (Spinner) adapterView;
                    showToast(Integer.toString(spinner.getSelectedItemPosition()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            adapter.setNotifyOnChange(true);


            ProductItem item = getItem(position);
            nameView.setText(item.name);
            priceView.setText(String.valueOf(item.price));
//            quantityView.setText(String.valueOf(item.num));
            return view;

        }

    }

    private class ProductDbItem {
        String name;
        int price;
        int num;
    }

    private List<ProductDbItem> itemDbList;

    private void setProductDbData(){

//        itemDbList = new ArrayList<ProductDbItem>();
//
//        ProductDbItem item = new ProductDbItem();
//        item.name = "赤鉛筆";
//        item.price = 500;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "青鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "黄鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "緑鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "紫鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "茶色鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "青鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "黄鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "緑鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "紫鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "茶色鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "青鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "黄鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "緑鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "紫鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);
//
//        item = new ProductDbItem();
//        item.name = "茶色鉛筆";
//        item.price = 200;
//        item.num = 1;
//        itemDbList.add(item);

    }

    private void initTable(){

        Log.d("ProductList", "initTable");

        SQLiteDatabase db = myHelper.getWritableDatabase();

        // 一旦削除
        int count = db.delete(Database.TABLE_NAME, null, null);
        Log.d("initTable", "count =" + count);

        setProductDbData();

        for(int i = 0; i< itemDbList.size(); i++){
            ProductDbItem item = itemDbList.get(i);

            // 列に対応する値をセットする
            ContentValues values = new ContentValues();
            values.put(Database.Columns.NAME, item.name);
            values.put(Database.Columns.PRICE, item.price);
            values.put(Database.Columns.NUM, item.num);

            // データベースに行を追加する
            long id = db.insert(Database.TABLE_NAME, null, values);
            if(id == -1){
                Log.d("Database", "行の追加に失敗したよ");
            }
        }

    }


}
