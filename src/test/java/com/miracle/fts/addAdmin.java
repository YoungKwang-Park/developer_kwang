package com.miracle.fts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*
 * 스프링에서는 자동으로 클래스 간 여러 의존주입이 발생하기 때문에
 * 다른 클래스에서 작성한 코드들을 사용할 수 있지만, JUnit 은 테스트 케이스 부분만 실행하기 때문에
 * 빈 자동 등록이나 의존주입이 일어나지 않습니다.
 * 따라서! @Service 나 @Mapper 가 붙은 클래스나 인터페이스도 쓰지 못하게 됩니다.
 * 이럴때는
 * @RunWith와 @ContextConfiguration을 이용합니다
 * 
 * 위와 같이 하면 JUnit 테스트를 실행할때 
 * @RunWith의 SpringJUnit4ClassRunner 클래스가 
 * @ContextConfiguration에 적어 놓은 파일들을 같이 실행시킵니다. 
 * root-context.xml과 security-context.xml을 실행시켜 빈 등록과 의존 주입을 실행시키는 것입니다
 */

// 참조 https://webcoding.tistory.com/entry/Spring-%EC%8A%A4%ED%94%84%EB%A7%81-JUnit%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%98%EC%97%AC-%EC%BD%94%EB%93%9C-%ED%85%8C%EC%8A%A4%ED%8A%B8%ED%95%98%EA%B8%B0

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
  "file:src/main/webapp/WEB-INF/spring/root-context.xml",
  "file:src/main/webapp/WEB-INF/spring/appServlet/security-context.xml"
  })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)  // JUnit 의 실행 순서를 메소드 이름 순으로 
public class addAdmin {

	// 자동 주입 받을 PasswordEncoder 와 DataSource 객체
	private PasswordEncoder pwencoder;
	private DataSource ds;
  
	public PasswordEncoder getPwencoder() {return pwencoder;}
	@Autowired
	public void setPwencoder(PasswordEncoder pwencoder) {this.pwencoder = pwencoder;}
	public DataSource getDs() {return ds;}
	@Autowired
	public void setDs(DataSource ds) {this.ds = ds;}


	Connection conn = null;
	PreparedStatement pstmt = null;
	final String SQL_INSERT_ADMIN = "INSERT INTO t_user (u_id, u_pw, u_name) VALUES (?,?,?)";
	final String SQL_INSERT_au_auth = "INSERT INTO t_auth (u_id, au_auth) VALUES (?,?)";
 
	@Before // org.junit.Before
	public void initialize() {
		System.out.println("AdminTests 시작");
		try {
			conn = ds.getConnection();   // DataSource 에서 Connection 받아옴.
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	} // end initialize()
  
	@Test
	public void testA_InsertMember() {
		System.out.println("testA_InsertAdmin() 실행");
		
if(conn == null) return;
		
		int cnt = 0;
		
		String u_id = "", u_pw = "", u_name = "";
	
		try {
			pstmt = conn.prepareStatement(SQL_INSERT_ADMIN);
			for(int i = 1; i < 8; i++) {
				u_id = "admin" + i;
				u_pw = "pw" + i;  
				u_name = "관리자" + i;

				cnt = 0;
				try {
					pstmt.setString(1, u_id);
					pstmt.setString(2, pwencoder.encode(u_pw));  // 패스워드 인코딩 
					pstmt.setString(3, u_name);
					cnt = pstmt.executeUpdate();
				} catch(Exception e) {
					System.out.println(e.getMessage());
				}
				
				if(cnt > 0) {
					System.out.println("INSERT_ADMIN 성공]" + u_id + ":" + u_pw + ":" + u_name);
				} else {
					System.out.println("INSERT_ADMIN 실패]" + u_id + ":" + u_pw + ":" + u_name);
				}
				
			} // end for
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
    
  } // end testInsertMember()
  
  @Test
  public void testB_Insertau_auth() {
    System.out.println("testB_Insert_auth() 실행");
    
    if(conn == null) return;
	
	int cnt = 0;
	String u_id = "", au_auth = "";
	
	try {
		pstmt = conn.prepareStatement(SQL_INSERT_au_auth);

		for(int i = 1; i < 8; i++) {
				u_id = "admin" + i;
				au_auth = "ROLE_ADMIN";
			cnt = 0;
			
			try {
				pstmt.setString(1, u_id);
				pstmt.setString(2, au_auth);
				cnt = pstmt.executeUpdate();	
				
				if(cnt > 0) {
					System.out.println("INSERT_au_auth 성공] " + u_id + ":" + au_auth);
				} else {
					System.out.println("INSERT_au_auth 실패] " + u_id + ":" + au_auth);
				}
				
				// admin 의 경우 ROLE_MEMBER 도 추가
				if(u_id.startsWith("admin")) {
					au_auth = "ROLE_MEMBER"; 
					pstmt.setString(1, u_id);
					pstmt.setString(2, au_auth);
					cnt = pstmt.executeUpdate();
					
					if(cnt > 0) {
						System.out.println("INSERT_au_auth 성공] " + u_id + ":" + au_auth);
					} else {
						System.out.println("INSERT_au_auth 실패] " + u_id + ":" + au_auth);
					}
				}
				
				// admin 의 경우 ROLE_CEO 도 추가
				if(u_id.startsWith("admin")) {
					au_auth = "ROLE_CEO"; 
					pstmt.setString(1, u_id);
					pstmt.setString(2, au_auth);
					cnt = pstmt.executeUpdate();
					
					if(cnt > 0) {
						System.out.println("INSERT_au_auth 성공] " + u_id + ":" + au_auth);
					} else {
						System.out.println("INSERT_au_auth 실패] " + u_id + ":" + au_auth);
					}
				}
				
			} catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}

		
		
	} catch(Exception e) {
		e.printStackTrace();
	} finally {
		try {
			if(pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
  } // end testInsertau_auth()
  
  @After  //org.junit.After;
  public void finalize() {
	  System.out.println("AdminTests 종료");
		try {
			if(conn != null) {
				conn.close();
				conn = null;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
  }
  
  
} // end class
