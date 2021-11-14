package com.blissstock.mappingSite.config;

import com.blissstock.mappingSite.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * SpringSecurityを利用するための設定クラス
 * ログイン処理でのパラメータ、画面遷移や認証処理でのデータアクセス先を設定する
 * @author aoi
 *
 */
@Configuration
//@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	//フォームの値と比較するDBから取得したパスワードは暗号化されているのでフォームの値も暗号化するために利用
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		return bCryptPasswordEncoder;
	}
	
	@Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler(){
        return new MySimpleUrlAuthenticationSuccessHandler();
    }
	
	/**
	 * 認可設定を無視するリクエストを設定
	 * 静的リソース(image,javascript,css)を認可処理の対象から除外する
	 */
	@Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                            "/images/**",
                            "/css/**",
                            "/javascript/**"
                            );
    }
	
	/**
	 * 認証・認可の情報を設定する
	 * 画面遷移のURL・パラメータを取得するname属性の値を設定
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		    .authorizeRequests()
		    .antMatchers("/ST0000_email_check").permitAll()
		    .antMatchers("/ST0001_register").permitAll();
		    
		  /*   .formLogin()
		        .loginPage("/N00001_Login") //ログインページはコントローラを経由しないのでViewNameとの紐付けが必要
		        .loginProcessingUrl("/sign_in") //フォームのSubmitURL、このURLへリクエストが送られると認証処理が実行される
		        .usernameParameter("txtuserName") //リクエストパラメータのname属性を明示
		        .passwordParameter("pwdpassword")
		        .successHandler(myAuthenticationSuccessHandler())
		        .failureUrl("/N00001_Login?error")//ログインURL失敗した時実行する
		        .permitAll()
		        .and() */
		    
		 http.csrf().disable();
	}
	
	/**
	 * 認証時に利用するデータソースを定義する設定メソッド
	 * ここではDBから取得したユーザ情報をuserDetailsServiceへセットすることで認証時の比較情報としている
	 * @param auth
	 * @throws Exception
	 */
	@Autowired
	public void configure(AuthenticationManagerBuilder auth) throws Exception{
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());

	}

}
