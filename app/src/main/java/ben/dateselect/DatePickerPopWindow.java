package ben.dateselect;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * PopWindow for Date Pick
 */
public class DatePickerPopWindow extends PopupWindow implements OnClickListener {

	private FrameLayout mFlCanecl;
	private FrameLayout mFlConfirm;
	private LoopView yearLoopView;
	private LoopView monthLoopView;
	private LoopView dayLoopView;
	private View contentView;//root view

	private int minYear; // min year
	private int maxYear; // max year
	private int yearPos = 0;
	private int monthPos = 0;
	private int dayPos = 0;
	private Context mContext;
	private TextView mTvDateStart;
	private TextView mTvDateEnd;
	private TextView mTvByMonth;

	private TextView mTvSwitch;
	private AppCompatImageView mIvClear;
	private RVFilterAdapter mAdapter;
	private RelativeLayout mRlFilter;
	private RecyclerView mRvFilter;
	private String mInitialDateStart;
	private String mInitialDateEnd;
	private String mInitialMonth;
	private FrameLayout mFlDatePicker;
	private FrameLayout mFlDatePickerByMonth;
	private LinearLayout mViewByDay;
	private LinearLayout mViewByMonth;


	public static class Builder {

		//Required
		private Context mContext;
		private final String mDateStart;
		private final String mDateEnd;
		private OnDatePickedListener mListener;
		private ArrayList<String> searchList;
		private boolean mSearch = true;

		public Builder(Context context, String dateStart, String dateEnd, OnDatePickedListener listener) {
			mContext = context;
			mDateStart = TextUtils.isEmpty(dateStart) ? "" : dateStart;
			mDateEnd = TextUtils.isEmpty(dateEnd) ? "" : dateEnd;
			mListener = listener;
		}

		//Option
		private boolean showDayMonthYear = false;
		private int minYear = LibGlobal.MIN_YEAR;
		private int maxYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

		public Builder minYear(int minYear) {
			this.minYear = minYear;
			return this;
		}

		public Builder maxYear(int maxYear) {
			this.maxYear = maxYear;
			return this;
		}

		public DatePickerPopWindow build() {
			if (minYear > maxYear) {
				throw new IllegalArgumentException();
			}
			return new DatePickerPopWindow(this);
		}

		public Builder showDayMonthYear(boolean useDayMonthYear) {
			this.showDayMonthYear = useDayMonthYear;
			return this;
		}
	}

	private DatePickerPopWindow(Builder builder) {
		minYear = builder.minYear;
		maxYear = builder.maxYear;
		mContext = builder.mContext;
		mListener = builder.mListener;
		mInitialDateStart = builder.mDateStart;
		mInitialDateEnd = builder.mDateEnd;
		mInitialMonth = DateUtils.formatDateToYearMonth(DateUtils.getFirstDayOfMonth(DateUtils.getYear(), DateUtils
			.getMonth()));
		initView();
	}

	private OnDatePickedListener mListener;

	private void initView() {
		try {
			contentView = LayoutInflater.from(mContext).inflate(R.layout.layout_date_picker, null);
			mFlCanecl = (FrameLayout) contentView.findViewById(R.id.fl_cancel);
			mFlConfirm = (FrameLayout) contentView.findViewById(R.id.fl_confirm);
			mViewByDay = (LinearLayout) contentView.findViewById(R.id.view_by_day);
			mViewByMonth = (LinearLayout) contentView.findViewById(R.id.view_by_month);
			mTvSwitch = (TextView) contentView.findViewById(R.id.tv_switch);

			if (mTvSwitch.getText().equals(mContext.getResources().getString(R.string.by_month))) {
				mViewByMonth.setVisibility(View.GONE);
				mViewByDay.setVisibility(View.VISIBLE);
				initDatePickerByDay();
			} else {
				mViewByDay.setVisibility(View.GONE);
				mViewByMonth.setVisibility(View.VISIBLE);
				initDatePickerByMonth();
			}

			mFlCanecl.setOnClickListener(this);
			mFlConfirm.setOnClickListener(this);
			mTvSwitch.setOnClickListener(this);

			setTouchable(true);
			setFocusable(true);
			// setOutsideTouchable(true);
			setBackgroundDrawable(new BitmapDrawable());
			setAnimationStyle(R.style.AnimBottom);
			setContentView(contentView);
			setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
			setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
	}

	private void initDatePickerByDay() {
		try {
			if (mTvDateStart == null || mTvDateEnd == null) {
				mTvDateStart = contentView.findViewById(R.id.tv_start);
				mTvDateEnd = contentView.findViewById(R.id.tv_end);
				//set default date
				if (!TextUtils.isEmpty(mInitialDateStart) && !TextUtils.isEmpty(mInitialDateEnd)) {
					mTvDateStart.setText(mInitialDateStart);
					mTvDateEnd.setText(mInitialDateEnd);
				}
			}
			mIvClear = contentView.findViewById(R.id.iv_clear);
			mFlDatePicker = contentView.findViewById(R.id.fl_date_picker);
			mFlDatePicker.removeAllViews();

			//set checked listen
			mTvDateStart.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					v.setSelected(true);
					mTvDateEnd.setSelected(false);
					initLoopViewsByDay(mFlDatePicker, mTvDateStart);
				}
			});
			mTvDateEnd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					v.setSelected(true);
					mTvDateStart.setSelected(false);
					initLoopViewsByDay(mFlDatePicker, mTvDateEnd);
				}
			});

			mIvClear.setOnClickListener(this);
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
	}

	private void initDatePickerByMonth() {
		try {
			if (mTvByMonth == null) {
				mTvByMonth = contentView.findViewById(R.id.tv_by_month);
				//set default date
				if (!TextUtils.isEmpty(mInitialMonth)) {
					mTvByMonth.setSelected(false);
					mTvByMonth.setText(mInitialMonth);
				}
			}
			mIvClear = contentView.findViewById(R.id.iv_clear_by_month);
			mFlDatePickerByMonth = contentView.findViewById(R.id.fl_date_picker_by_month);
			mFlDatePickerByMonth.removeAllViews();

			//set checked listen
			mTvByMonth.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					v.setSelected(true);
					initLoopViewsByMonth(mFlDatePickerByMonth, mTvByMonth);
				}
			});

			mIvClear.setOnClickListener(this);
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
	}

	/**
	 * Init year and month loop view,
	 * Let the day loop view be handled separately
	 */
	private void initPickerViews() {
		try {
			List<String> yearList = new ArrayList<>();
			List<String> monthList = new ArrayList<>();

			int yearCount = maxYear - minYear;

			for (int i = 0; i < yearCount; i++) {
				yearList.add(format2LenStr(minYear + i));
			}

			for (int j = 0; j < 12; j++) {
				monthList.add(format2LenStr(j + 1));
			}

			yearLoopView.setDataList(yearList);
			yearLoopView.setInitPosition(yearPos);

			monthLoopView.setDataList(monthList);
			monthLoopView.setInitPosition(monthPos);
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
	}

	/**
	 * Init day item
	 */
	private void initDayPickerView() {
		try {
			int dayMaxInMonth;
			Calendar calendar = Calendar.getInstance();
			List<String> dayList = new ArrayList<>();

			calendar.set(Calendar.YEAR, minYear + yearPos);
			calendar.set(Calendar.MONTH, monthPos);

			//get max day in month
			dayMaxInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

			for (int i = 0; i < dayMaxInMonth; i++) {
				dayList.add(format2LenStr(i + 1));
			}

			dayLoopView.setDataList(dayList);
			dayLoopView.setInitPosition(dayPos);
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
	}

	/**
	 * set selected date position value when initView.
	 */
	private void setSelectedDate(String dateStr) {
		try {
			if (!TextUtils.isEmpty(dateStr)) {
				long milliseconds = getLongFromyyyyMMdd(dateStr);
				Calendar calendar = Calendar.getInstance(Locale.CHINA);

				if (milliseconds != -1) {
					calendar.setTimeInMillis(milliseconds);
					yearPos = calendar.get(Calendar.YEAR) - minYear;
					monthPos = calendar.get(Calendar.MONTH);
					dayPos = calendar.get(Calendar.DAY_OF_MONTH) - 1;
				}
			}
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
	}

	private void setSelectedDateByMonth(String dateStr) {
		try {
			if (!TextUtils.isEmpty(dateStr)) {
				long milliseconds = getLongFromyyyyMM(dateStr);
				Calendar calendar = Calendar.getInstance(Locale.CHINA);

				if (milliseconds != -1) {
					calendar.setTimeInMillis(milliseconds);
					yearPos = calendar.get(Calendar.YEAR) - minYear;
					monthPos = calendar.get(Calendar.MONTH);
				}
			}
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
	}

	/**
	 * Show date picker popWindow
	 */
	public void showPopWin(final Activity activity) {
		try {
			if (null != activity) {
				setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss() {
						WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
						lp.alpha = 1f;
						activity.getWindow().setAttributes(lp);
					}
				});

				showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);

				//虚化背景
				WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
				lp.alpha = 0.7f;
				activity.getWindow().setAttributes(lp);
			}
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
	}

	@Override
	public void onClick(View v) {
		try {
			if (v == mFlCanecl) {
				dismiss();
			} else if (v == mFlConfirm) {
				if (null != mListener) {
					if (mTvSwitch.getText().equals(mContext.getResources().getString(R.string.by_month))) {
						if (!mTvDateStart.getText()
							.equals(mContext.getResources()
								.getString(R.string.start_date)) && !mTvDateEnd.getText()
							.equals(mContext.getResources().getString(R.string.end_date))) {
							mListener.onDatePickCompleted(mTvDateStart.getText().toString(), mTvDateEnd.getText()
								.toString());
							dismiss();
						} else {
							Toast.makeText(mContext, R.string.please_select_a_valid_date, Toast.LENGTH_LONG).show();
						}
					} else {
						if (!mTvByMonth.getText()
							.equals(mContext.getResources().getString(R.string.by_month))) {
							int year = getYear(mTvByMonth.getText().toString());
							int month = getMonth(mTvByMonth.getText().toString());
							mListener.onDatePickCompleted(DateUtils.getFirstDayOfMonth(year, month), DateUtils.getLastDayOfMonth(year, month));
							dismiss();
						} else {
							Toast.makeText(mContext, R.string.please_select_a_valid_date, Toast.LENGTH_LONG).show();
						}
					}
				}
			} else if (v == mIvClear) {
				if (mTvSwitch.getText().equals(mContext.getResources().getString(R.string.by_month))) {
					mTvDateStart.setSelected(false);
					mTvDateEnd.setSelected(false);
					mTvDateStart.setText(mInitialDateStart);
					mTvDateEnd.setText(mInitialDateEnd);
					mFlDatePicker.removeAllViews();
				} else {
					mTvByMonth.setSelected(false);
					mTvByMonth.setText(mInitialMonth);
					mFlDatePickerByMonth.removeAllViews();
				}
			} else if (v == mTvSwitch) {
				if (mTvSwitch.getText().equals(mContext.getResources().getString(R.string.by_month))) {
					mTvDateStart.setSelected(false);
					mTvDateEnd.setSelected(false);
					mTvSwitch.setText(R.string.by_day);
					initDatePickerByMonth();
					mViewByDay.setVisibility(View.GONE);
					mViewByMonth.setVisibility(View.VISIBLE);
				} else {
					mTvByMonth.setSelected(false);
					mTvSwitch.setText(R.string.by_month);
					initDatePickerByDay();
					mViewByMonth.setVisibility(View.GONE);
					mViewByDay.setVisibility(View.VISIBLE);
				}
			}
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
	}

	private String getFullDate() {
		try {
			int year = minYear + yearPos;
			int month = monthPos + 1;
			int day = dayPos + 1;
			return String.format("%s-%s-%s", String.valueOf(year), format2LenStr(month), format2LenStr(day));
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
		return null;
	}

	private String getFullDateByMonth() {
		try {
			int year = minYear + yearPos;
			int month = monthPos + 1;
			return String.format("%s-%s", String.valueOf(year), format2LenStr(month));
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
		return null;
	}

	private static int getYear(String date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH);
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(date));
			return cal.get(Calendar.YEAR);
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
			return 0;
		}
	}

	private static int getMonth(String date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH);
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(date));
			return cal.get(Calendar.MONTH) + 1;
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
			return 0;
		}
	}

	/**
	 * get long from yyyy-MM-dd
	 */
	private static long getLongFromyyyyMMdd(String date) {
		try {
			SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			Date parse = mFormat.parse(date);
			if (parse != null) {
				return parse.getTime();
			} else {
				return -1;
			}
		} catch (ParseException e) {
			FileUtils.addErrorLog(e);
			return -1;
		}
	}

	/**
	 * get long from yyyy-MM
	 */
	private static long getLongFromyyyyMM(String date) {
		try {
			SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
			Date parse = mFormat.parse(date);
			if (parse != null) {
				return parse.getTime();
			} else {
				return -1;
			}
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
			return -1;
		}
	}

	private static String getStrDate() {
		SimpleDateFormat dd = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		return dd.format(new Date());
	}

	/**
	 * Transform int to String with prefix "0" if less than 10
	 */
	private static String format2LenStr(int num) {
		return (num < 10) ? "0" + num : String.valueOf(num);
	}

	public static int spToPx(Context context, int spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}


	public interface OnDatePickedListener {
		/**
		 * Listener when date has been checked
		 *
		 * @param dateStart yyyy-MM-dd
		 * @param dateEnd   yyyy-MM-dd
		 */
		void onDatePickCompleted(String dateStart, String dateEnd);
	}

	private void initLoopViewsByDay(FrameLayout contentView, final TextView tvDate) {
		try {
			String selectedDate = tvDate.getText().toString();
			if (TextUtils.isEmpty(selectedDate)) {
				selectedDate = DateUtils.getToday();
				tvDate.setText(selectedDate);
			}
			setSelectedDate(selectedDate);
			LinearLayout llDatePicker = (LinearLayout) LayoutInflater.from(mContext)
				.inflate(R.layout.view_loop_by_day, null);
			contentView.removeAllViews();
			contentView.addView(llDatePicker);
			yearLoopView = llDatePicker.findViewById(R.id.picker_year);
			monthLoopView = llDatePicker.findViewById(R.id.picker_month);
			dayLoopView = llDatePicker.findViewById(R.id.picker_day);
			initPickerViews(); // init year and month loop view
			initDayPickerView(); //init day loop view

			yearLoopView.setLoopListener(new LoopScrollListener() {
				@Override
				public void onItemSelect(int item) {
					yearPos = item;
					initDayPickerView();
					tvDate.setText(getFullDate());
				}
			});

			monthLoopView.setLoopListener(new LoopScrollListener() {
				@Override
				public void onItemSelect(int item) {
					monthPos = item;
					initDayPickerView();
					tvDate.setText(getFullDate());
				}
			});

			dayLoopView.setLoopListener(new LoopScrollListener() {
				@Override
				public void onItemSelect(int item) {
					dayPos = item;
					tvDate.setText(getFullDate());
				}
			});
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
	}

	private void initLoopViewsByMonth(FrameLayout contentView, final TextView tvDate) {
		try {
			setSelectedDateByMonth(tvDate.getText().toString());
			LinearLayout llDatePicker = (LinearLayout) LayoutInflater.from(mContext)
				.inflate(R.layout.view_loop_by_month, null);
			contentView.removeAllViews();
			contentView.addView(llDatePicker);
			yearLoopView = llDatePicker.findViewById(R.id.picker_year);
			monthLoopView = llDatePicker.findViewById(R.id.picker_month);
			initPickerViews(); // init year and month loop view

			yearLoopView.setLoopListener(new LoopScrollListener() {
				@Override
				public void onItemSelect(int item) {
					yearPos = item;
					tvDate.setText(getFullDateByMonth());
				}
			});

			monthLoopView.setLoopListener(new LoopScrollListener() {
				@Override
				public void onItemSelect(int item) {
					monthPos = item;
					tvDate.setText(getFullDateByMonth());
				}
			});
		} catch (Exception e) {
			FileUtils.addErrorLog(e);
		}
	}

	private class MyItemDecoration extends RecyclerView.ItemDecoration {
		private int leftRight;
		private int topBottom;

		MyItemDecoration(int leftRight, int topBottom) {
			this.leftRight = leftRight;
			this.topBottom = topBottom;
		}

		@Override
		public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
			try {
				StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) parent.getLayoutManager();
				//判断总的数量是否可以整除
				int totalCount = layoutManager.getItemCount();
				int surplusCount = totalCount % layoutManager.getSpanCount();
				int childPosition = parent.getChildAdapterPosition(view);
				if (surplusCount == 0 && childPosition > totalCount - layoutManager.getSpanCount() - 1) {
					//后面几项需要右边
					outRect.right = leftRight;
				} else if (surplusCount != 0 && childPosition > totalCount - surplusCount - 1) {
					outRect.right = leftRight;
				}

				outRect.top = topBottom;
				outRect.bottom = topBottom;

				outRect.left = leftRight;
			} catch (Exception e) {
				FileUtils.addErrorLog(e);
			}
		}
	}
}
