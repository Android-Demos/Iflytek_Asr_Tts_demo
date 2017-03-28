package com.tangao.test;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import jxl.Sheet;
import jxl.Workbook;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "MainActivity";
    private static final String APPID = "58d9cb79";

    private TextView etText;
    private Button btnStartAsr;
    private EditText etHeCheng;
    private Button btnStartTTs;
    private TextView mResultTv;

    //有动画效果
    private RecognizerDialog iatDialog;
    //无动画效果
    private SpeechRecognizer mIat;
    //HashMap用来存放excel表格中的兼职匹配
    private ConcurrentHashMap<String, String> mResultMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 语音配置对象初始化(如果只使用 语音识别 或 语音合成 时都得先初始化这个)
        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID + "=" + APPID);

        initData();
        initView();
    }

    private void initView() {
        etText = (TextView) findViewById(R.id.main_et_text);
        btnStartAsr = (Button) findViewById(R.id.main_btn_startSpeak);
        btnStartAsr.setOnClickListener(this);
        etHeCheng = (EditText) findViewById(R.id.main_et_needToHeCheng);
        btnStartTTs = (Button) findViewById(R.id.main_btn_startHeCheng);
        btnStartTTs.setOnClickListener(this);
        mResultTv = (TextView) findViewById(R.id.result);
    }


    /**
     * 用于SpeechRecognizer（无交互动画）对象的监听回调
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }

        @Override
        public void onBeginOfSpeech() {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            Log.i(TAG, recognizerResult.toString());
        }

        @Override
        public void onError(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };


    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(MainActivity.this, "初始化失败，错误码：" + code, Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 开始科大讯飞的合成语音
     */
    private void startHeCheng(String recordResult) {
        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer(MainActivity.this, null);

        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);

        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "55");//设置语速
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        //3.开始合成
        int code = mTts.startSpeaking(recordResult, mSynListener);

        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                //上面的语音配置对象为初始化时：
                Toast.makeText(MainActivity.this, "语音组件未安装", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "语音合成失败,错误码: " + code, Toast.LENGTH_LONG).show();
            }
        }

    }

    //合成监听器
    private SynthesizerListener mSynListener = new SynthesizerListener() {
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {
            Log.e(TAG, error.toString());
        }

        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
        }

        //开始播放
        public void onSpeakBegin() {
        }

        //暂停播放
        public void onSpeakPaused() {
        }

        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        //恢复播放回调接口
        public void onSpeakResumed() {
        }

        //会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_btn_startSpeak:
                startAsr();
                break;
            case R.id.main_btn_startHeCheng:
                if (!TextUtils.isEmpty(getResult(etHeCheng.getText().toString()))) {
                    mResultTv.setText(getResult(etHeCheng.getText().toString()));
                    startHeCheng(getResult(etHeCheng.getText().toString()));
                } else {
                    startHeCheng(etHeCheng.getText().toString());
                }
                break;

        }
    }

    private void startAsr() {
        // 有交互动画的语音识别器
        iatDialog = new RecognizerDialog(MainActivity.this, mInitListener);
        //1.创建SpeechRecognizer对象(没有交互动画的语音识别器)，第2个参数：本地听写时传InitListener
        mIat = SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);
        // 2.设置听写参数
        mIat.setParameter(SpeechConstant.DOMAIN, "iat"); // domain:域名
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin"); // mandarin:普通话
        //保存音频文件到本地（有需要的话）   仅支持pcm和wav
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/mIat.wav");
        //mIat.startListening(mRecognizerListener);

        iatDialog.setListener(new RecognizerDialogListener() {
            String resultJson = "[";//放置在外边做类的变量则报错，会造成json格式不对（？）

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                System.out.println("-----------------   onResult   -----------------");
                if (!isLast) {
                    resultJson += recognizerResult.getResultString() + ",";
                } else {
                    resultJson += recognizerResult.getResultString() + "]";
                }

                if (isLast) {
                    //解析语音识别后返回的json格式的结果
                    Gson gson = new Gson();
                    List<DictationResult> resultList = gson.fromJson(resultJson,
                            new TypeToken<List<DictationResult>>() {
                            }.getType());
                    String result = "";
                    for (int i = 0; i < resultList.size() - 1; i++) {
                        result += resultList.get(i).toString();
                    }
                    etText.setText(result);
                    //获取焦点
                    etText.requestFocus();
                    mResultTv.setText(getResult(result));
                    startHeCheng(getResult(result));

                }
            }

            @Override
            public void onError(SpeechError speechError) {
                //自动生成的方法存根
                speechError.getPlainDescription(true);
            }
        });
        //开始听写，需将sdk中的assets文件下的文件夹拷入项目的assets文件夹下（没有的话自己新建）
        iatDialog.show();
    }

    /**
     * 根据名字获取解释
     *
     * @param str
     * @return
     */
    private String getResult(String str) {
        return mResultMap.get(str);
    }

    /**
     * 初始化excel表格数据到hashMap
     */
    private void initData() {
        mResultMap = new ConcurrentHashMap<>();
        try {

            Workbook workbook = Workbook.getWorkbook(getAssets().open("demohs.xls"));
            Sheet sheet = workbook.getSheet(0);

            int sheetNum = workbook.getNumberOfSheets();
            int sheetRows = sheet.getRows();
            int sheetColumns = sheet.getColumns();

            Log.d(TAG, "the num of sheets is " + sheetNum);
            Log.d(TAG, "the name of sheet is  " + sheet.getName());
            Log.d(TAG, "total rows is 行=" + sheetRows);
            Log.d(TAG, "total cols is 列=" + sheetColumns);

            for (int i = 0; i < sheetRows; i++) {
                mResultMap.put(sheet.getCell(0, i).getContents(), sheet.getCell(1, i).getContents());
                Log.d(TAG, sheet.getCell(0, i).getContents() + " " + sheet.getCell(1, i).getContents());
            }

            workbook.close();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "未找到xls文件");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "read error=" + e, e);
        }
    }
}