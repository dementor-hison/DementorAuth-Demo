package kr.co.dementor.dementorauth_demo;

import kr.co.dementorauth.DementorConfig;
import kr.co.dementorauth.DementorRegController;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class RegisterActivity extends FragmentActivity implements DementorRegController.OnDementerRegisterListener {
	private DementorRegController mRegController;
	private TextView mTitle;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.a_reg_activity);
		
		mTitle = (TextView)this.findViewById(R.id.txtTitle);
		mTitle.setText("그래픽인증 등록");
		
		ViewGroup vg = (ViewGroup)this.findViewById(R.id.reg_view);

		this.findViewById(R.id.layoutBack).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// [뒤로가기]
						// 사용자가 입력한 데이터가 있을때는 입력데이터를 초기화하고
						// 없을때는 
						// 1) 비밀번호 확인 화면일 경우 그래픽인증 등록 화면으로 이동,
						// 2) 그래픽인증 등록화면일 경우 needFinish()를 호출한다.
						mRegController.back();
					}
				});

		this.findViewById(R.id.layoutSetting).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// [도움말 팝업]
						mRegController.showHelp();
					}
				});
		
		// [디멘터 설정 생성]
		DementorConfig config = new DementorConfig(
				MainActivity.mHost,  	// 접속 주소 
				MainActivity.mUserID, 	// 아이디1
				MainActivity.mDeviceID);	// 아이디2
		config.getConnectInfo().setPath("dmtd");		// 접속 path
		
		config.setHighlightColor(Color.rgb(109, 195, 151));	// Highlight 색상
		config.setDisableColor(Color.rgb(150, 149, 149));	// Disable 색상
		config.setIconBackgroundColor(Color.rgb(191, 225, 208));	// 아이콘의 배경 색상
		
		// [디멘터 컨트롤러 생성]
		mRegController = new DementorRegController(
				this, 								// Context
				this.getSupportFragmentManager(), 	// FragmentManager
				config);								// [디멘터 설정 초기화]에서 생성한 config
		mRegController.setOnDementerRegisterListener(this);	// 컨트롤러에서 이벤트 응답을 받기 위한 Listener
		
		// [그래픽인증 등록 View add]
		vg.addView(mRegController.getView());
		
		// [그래픽인증 등록 시작]
		mRegController.start();
	}

	@Override
	protected void onDestroy() {
		// [메모리 정리]
		// 이 화면이 종료될때 Bitmap에 의해 점유된 메모리를 정리하기 위하여 
		// 반드시 호출되어야합니다.
		mRegController.destroy();
		
		super.onDestroy();
	}

	// [DementorRegController.OnDementerRegisterListener]
	@Override
	// 그래픽인증 등록 > 비밀번호 확인 화면으로 전환되었을때 호출됨
	public void onConfirmFragmentAttached() {
		mTitle.setText("비밀번호 확인");
	}
	
	@Override
	// 비밀번호 확인 화면이 닫히고 그래픽인증 등록 화면으로 돌아갈때 호출됨
	public void onConfirmFragmentDeattaced() {
		mTitle.setText("그래픽인증 등록");
	}

	@Override
	// 사용자에 의해 화면이 종료되야 할 때 호출됨(상단의 [뒤로가기] 부분이 계속하여 호출되었을 경우)
	public void needFinish() {
		this.finish();
	}

	@Override
	// [진행 종료]
	// 진행중 오류가 발생하였거나 사용자의 입력이 완료되었을때 결과 및 오류 코드와 함께 호출됨
	public void onRegisterFinished(String verificationCode, String errorCode, String errorInfo) {
		// verificationCode은 디멘터 서버와 상호 검증이 필요할 때 사용
		// errorCode가 null일 경우 성공
		// errorCode가 null이 아닐 경우는 실패, 코드에 따라 사후 처리 필요
		// 오류코드가 20으로 시작하는 것은 라이브러리에서 통신 실패시 발생함으로 [재시도] 진행 필요
		AlertDialog.Builder builder = new AlertDialog.Builder(this);		
		builder.setTitle("결과 확인")       
			.setMessage("[Verification Code]\n" + verificationCode +
					"\n\n[Error Code]\n" + errorCode +
					"\n\n[Error Info]\n" + errorInfo)           
			.setPositiveButton("확인", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				}
			})
			.setNegativeButton("재시도", new DialogInterface.OnClickListener() {      
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
					
					// [재시도]
					// 마지막 실행했던 통신을 재시도
					mRegController.retry();
				}
			});
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
