package ben.dateselect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private DatePickerPopWindow mDatePickerPopWindow;
	private String mDateStart = "";
	private String mDateEnd = "";
	private AppCompatImageView mIvDatePicker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initListener();
	}

	private void initView() {
		mIvDatePicker = findViewById(R.id.iv_date_picker);

		initDatePicker();
	}

	private void initListener() {
		mIvDatePicker.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDatePickerPopWindow.showPopWin(MainActivity.this);
			}
		});
	}

	private void initDatePicker() {
		try {
			mDatePickerPopWindow = new DatePickerPopWindow.Builder(MainActivity.this, mDateStart, mDateEnd, new DatePickerPopWindow.OnDatePickedListener() {
				@Override
				public void onDatePickCompleted(String dateStart, String dateEnd) {
					mDateStart = dateStart;
					mDateEnd = dateEnd;
					//do some things
					String str = "Start: " + mDateStart + ", End: " + mDateEnd;
					Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
				}
			}).minYear(LibGlobal.MIN_YEAR).build();
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
	}
}
