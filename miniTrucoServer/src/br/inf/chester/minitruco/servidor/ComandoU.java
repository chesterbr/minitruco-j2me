package br.inf.chester.minitruco.servidor;

/*
 * Copyright © 2006-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Copyright (c) 2007-2009 Sandro Gasparotto (sandro.gasparoto@gmail.com)
 * 
 * Este programa é um software livre; você pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença, ou (na sua opnião) qualquer versão.
 *
 * Este programa é distribuido na esperança que possa ser util, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO
 * a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença
 * Pública Geral GNU para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU
 * junto com este programa, se não, escreva para a Fundação do Software
 * Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.security.*;
import java.math.*;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

/**
 * Interage com o banco de dados (mySQL)
 *
 * Parâmetro: 	SET USERNAME: SU email pass [invitation_code] (creates/adds user in the database)
 *					reply: X DB, others (error) / U SU
 *				
 *				DELETE USERNAME: DX email pass (deletes user from database)
 *					reply: X DB, others (error) / U DX
 *
 *				SET PREFS: SP city state birth sex avatar (sets preferences)
 *					reply: X DB, others (error) / U SP city|state|birth|sex|avatar
 *
 *				SET PASSWORD: SS pass (sets new password)
 *					reply: X DB, others (error) / U SS 
 *
 *				SET EMAIL: SE email (sets new email)
 *					reply: X DB, others (error) / U SE 
 *
 *				SET COOL: SC username (increments field 'cool')
 *					reply: X DB, others (error) / U SC username
 *				
 *				GET INFO: GI username (gets information about an user)
 *					reply: X DB, others (error) / U GI username|creation|city|state|birth|sex|avatar|wins|losses|cool
 *
 *				RESET STATS: RS 
 *					reply: X DB, others (error) / U RS
 *
 *				FORGOT PASSWORD: FP email (resets password and sends out an email) 
 *					reply: X DB, others (error) / U FP
 *
 * @author Chester, Sandro
 * 
 */
public class ComandoU extends Comando {

  private static final String CARACTERES_PERMITIDOS = "!@$()-_.";
	
	@Override
	public void executa(String[] args, JogadorConectado j) {
		String emailMsgTxt;
		String emailSubjectTxt;
		String emailFromAddress;
		String[] emailList = {""};
		String reply = "X DB"; // in case U command is not well formatted
		try{
			// which command?
			if(args[1].equalsIgnoreCase("SU")) {
				if (j.getNome().equals("unnamed")) {
					j.println("X NO");
					return;
				}
				// check parameters
				if(args[2].length() > 0 && args[2].length() <= 50 &&
						args[3].length() > 0 && args[3].length() <= 32
						) {
					// Valida o email
					for (int i = 0; i < args[2].length(); i++) {
						char c = args[2].charAt(i);
						if (!(Character.isLetterOrDigit(c) || CARACTERES_PERMITIDOS
								.indexOf(c) != -1)) {
							j.println("X NX");
							return;
						}
					}
					
					// check invitation code
					if (MiniTrucoServer.SERVER.get("INVITATION_CODE_ENABLED").equals("TRUE")) {
						if(args[4].length() > 0 && args[4].length() <=10) {
							String query = "SELECT available FROM invitations WHERE code=\"" + args[4] + "\"";
							reply = j.mySQL(query);
							if (reply.equals("ERROR")) {
								j.println("X NC");
								return;
							}
							if (reply.equals("0")) {
								j.println("X NX");
								return;
							}
						} else {
							j.println("X NC");
							return;						
						}
					}
					
					// check for duplicated email
					// format query
					String query = "SELECT * FROM users WHERE email=\"" + args[2] + "\"";
					reply = j.mySQL(query);
					if (!reply.equals("ERROR")) {
						// ops, somebody already has this same email
						j.println("X NA");
						return;
					}
				    String password = args[3];
				    // date
					Calendar cal = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String date = sdf.format(cal.getTime());
					//format query
					query = "INSERT INTO users (username, password, email, creation, last_login) VALUES (\"" + j.getNome() + "\",\"" + password +"\",\"" + args[2] +"\",\"" + date +"\",\"" + date +"\")";
					reply = "U " + j.mySQL(query);
					if (!reply.equals("U OK")) {
						reply = "X DB";
					} else {
						// invitation_code not to be used
						//query = "UPDATE invitations SET available=available-1 WHERE code=\"" + args[4] + "\"";
						//String dummy = j.mySQL(query); // dummy not used
						// user is not guest anymore
						j.setIsGuest(false);
						j.removeConvidado(j.getNome());
						// check login time
						j.setLoginTime(System.currentTimeMillis());
						// format response
						reply = "U SU";
						// sends out an email
						try {
							emailMsgTxt      = MiniTrucoServer.SERVER.get("REG_EMAIL_MSG_TXT");
							emailSubjectTxt  = MiniTrucoServer.SERVER.get("REG_EMAIL_SUBJECT_TXT");
							emailFromAddress = MiniTrucoServer.SERVER.get("SMTP_AUTH_USER");
							emailList[0] = args[2];
							Runnable runnable = new postMail(emailList, emailSubjectTxt, emailMsgTxt, emailFromAddress);
							Thread t = new Thread(runnable);
							t.start();
						} catch (Exception e) {
							System.out.println(e.toString());
						}
					}
				}	
			}
			if(args[1].equalsIgnoreCase("DX")) {
				if (j.getNome().equals("unnamed")) {
					j.println("X NO");
					return;
				}
				if (j.getIsGuest()) {
					j.println("X NG");
					return;
				}
				// check parameters
				if(args[2].length() > 0 && args[2].length() <= 50 &&
						args[3].length() > 0 && args[3].length() <= 32
						) {
					// check for credentials
					String query = "SELECT * FROM users WHERE email=\"" + args[2] + "\" AND password=\"" + args[3] + "\"";
					reply = j.mySQL(query);
					if (reply.equals("ERROR")) {
						j.println("X NP");
						return;
					}
					//format query
					query = "DELETE FROM users WHERE email=\"" + args[2] + "\" AND password=\"" + args[3] + "\"";
					reply = "U " + j.mySQL(query);
					if (!reply.equals("U OK")) {
						reply = "X DB";
					} else {
						// user is guest now
						j.setIsGuest(true);
						j.adicionaConvidado(j.getNome());
						// format response
						reply = "U DX";
					}
				}	
			}
			if(args[1].equalsIgnoreCase("SP")) {
				if (j.getNome().equals("unnamed")) {
					j.println("X NO");
					return;
				}
				if (j.getIsGuest()) {
					j.println("X NG");
					return;
				}
				// check parameters
				String city = args[2];
				String state = args[3];
				String birth = args[4];
				String sex = args[5];
				int avatar = Integer.parseInt(args[6]);
				// for 'blank', we use '-'
				if (city.equals("-")) city="";
				if (state.equals("-")) state="";
				if (sex.equals("-")) sex="";
				if(city.length() <= 15 &&
						state.length() <= 2 &&
						birth.length() > 0 && birth.length() <= 10 &&
						sex.length() <= 1 &&
						avatar > 0 && avatar <= 100000) {
					//format query
					String query = "UPDATE users SET city=\"" + city + "\",state=\"" + state + "\",birth=\"" + birth + "\",sex=\"" + sex + "\",avatar=" + avatar + " WHERE username=\"" + j.getNome() + "\"";
					reply = "U " + j.mySQL(query);
					if (!reply.equals("U OK")) reply = "X DB"; else {
						// format response
						reply = "U SP " + city +"|" + state +"|" + birth +"|" + sex +"|" + avatar;
					}
				}
			}
			if(args[1].equalsIgnoreCase("SS")) {
				if (j.getNome().equals("unnamed")) {
					j.println("X NO");
					return;
				}
				if (j.getIsGuest()) {
					j.println("X NG");
					return;
				}
				// check parameters
				String pass = args[2];
				if(pass.length() > 0 && pass.length() <= 32) {
					//format query
					String query = "UPDATE users SET password=\"" + pass + "\" WHERE username=\"" + j.getNome() + "\"";
					reply = "U " + j.mySQL(query);
					if (!reply.equals("U OK")) reply = "X DB"; else {
						// format response
						reply = "U SS";
					}
				}
			}
			if(args[1].equalsIgnoreCase("SE")) {
				if (j.getNome().equals("unnamed")) {
					j.println("X NO");
					return;
				}
				if (j.getIsGuest()) {
					j.println("X NG");
					return;
				}
				// check parameters
				String email = args[2];
				if(email.length() > 0 && email.length() <= 50) {
					// Valida o email
					for (int i = 0; i < email.length(); i++) {
						char c = email.charAt(i);
						if (!(Character.isLetterOrDigit(c) || CARACTERES_PERMITIDOS
								.indexOf(c) != -1)) {
							j.println("X NY");
							return;
						}
					}
					//checa se tem alguém no banco de dados com o mesmo email
					String query = "SELECT * FROM users WHERE email=\"" + email + "\"";
					if (!j.mySQL(query).equals("ERROR")) {
						j.println("X NR");
						return;
					}
					//format query
					query = "UPDATE users SET email=\"" + email + "\" WHERE username=\"" + j.getNome() + "\"";
					reply = "U " + j.mySQL(query);
					if (!reply.equals("U OK")) reply = "X DB"; else {
						// format response
						reply = "U SE "+email;
					}
				}
			}
			if(args[1].equalsIgnoreCase("SC")) {
				if (j.getNome().equals("unnamed")) {
					j.println("X NO");
					return;
				}
				if (j.getIsGuest()) {
					j.println("X NG");
					return;
				}
				// check parameters
				if(args[2].length() > 0 && args[2].length() <= 20) {
					// check if it's not self vote
					if (j.getNome().equals(args[2])) {
						j.println("X DB");
						return;
					}
					// check if he/she has not been voted yet
					int counter;
					for(counter = 0; counter < j.getVotedCoolListLength(); counter++) {
			            if(args[2].equals(j.getVotedCoolList(counter))) {
			            	j.println("X DV"); 
							return;			            	
			            }
			        }
					//format query
					String query = "UPDATE users SET cool = cool + 1 WHERE username=\"" + args[2] + "\"";
					reply = "U " + j.mySQL(query);
					if (!reply.equals("U OK")) reply = "X DB"; else {
						j.addToVotedCoolList(args[2]); // adds to the list of voted people (avoids double voting at least during this session...) NOTE: non-matching usernames also return OK, so they are added here... not a problem
						// format response
						reply = "U SC " + args[2];
					}
				}
			}
			if(args[1].equalsIgnoreCase("GI")) {
				if (j.getNome().equals("unnamed")) {
					j.println("X NO");
					return;
				}
				// check parameters
				if(args[2].length() > 0 && args[2].length() <= 20) {
					//format query
					String query = "SELECT * FROM users WHERE username=\"" + args[2] + "\"";
					reply = j.mySQL(query);
					if (reply.equals("ERROR")) {
						j.println("X NQ");
						return;
					}
					reply = "U " + reply;
				}		
			}
			if(args[1].equalsIgnoreCase("RS")) {
				if (j.getNome().equals("unnamed")) {
					j.println("X NO");
					return;
				}
				if (j.getIsGuest()) {
					j.println("X NG");
					return;
				}
				//format query
				String query = "UPDATE users SET wins=0,losses=0 WHERE username=\"" + j.getNome() + "\"";
				reply = "U " + j.mySQL(query);
				if (!reply.equals("U OK")) reply = "X DB"; else reply = "U RS";
			}
			if(args[1].equalsIgnoreCase("FP")) {
				// check parameters
				if(args[2].length() > 0 && args[2].length() <= 50) {
					// look for email in database
					String query = "SELECT * FROM users WHERE email=\"" + args[2] + "\"";
					reply = j.mySQL(query);
					if (reply.equals("ERROR")) {
						j.println("X NM");
						return;
					}
					// generates new password
					Random random = new Random();
					char[] chars = new char[]{'a','b','c','d','e','1','2','3','4','5'};
					String plainPassword = "";
					int r;
					for (int i=0; i<=6; i++) {
						r = random.nextInt(chars.length);
						plainPassword = plainPassword + chars[r];
					}
				    // MD5 hash
					MessageDigest m=MessageDigest.getInstance("MD5");
				    m.update(plainPassword.getBytes(),0,plainPassword.length());
				    String password = new BigInteger(1,m.digest()).toString(16);
				    if (password.length() == 31) {
				    	  password = "0" + password;
				    }
					//format query
					query = "UPDATE users SET password=\"" + password + "\" WHERE email=\"" + args[2] + "\"";
					reply = "U " + j.mySQL(query);
					if (!reply.equals("U OK")) reply = "X DB"; else {
						// sends out an email with new password
						try {
							emailMsgTxt      = MiniTrucoServer.SERVER.get("NEW_PASS_EMAIL_MSG_TXT_BEF_PASS") + plainPassword + MiniTrucoServer.SERVER.get("NEW_PASS_EMAIL_MSG_TXT_AFT_PASS");
							emailSubjectTxt  = MiniTrucoServer.SERVER.get("NEW_PASS_EMAIL_SUBJECT_TXT");
							emailFromAddress = MiniTrucoServer.SERVER.get("SMTP_AUTH_USER");
							emailList[0] = args[2];
							Runnable runnable = new postMail(emailList, emailSubjectTxt, emailMsgTxt, emailFromAddress);
							Thread t = new Thread(runnable);
							t.start();
						} catch (Exception e) {
							System.out.println(e.toString());
						}
						// format response
						reply = "U FP";
					}
				}
			}
			j.println(reply);
		} catch(Exception e) {
			j.println(reply);
		}
	}

	// envio de email foi implementado para ser lançado como uma thread
	// para não "hang on" o usuário
	class postMail implements Runnable
    {
		private String[] recipients;
		private String subject;
		private String message;
		private String from;
		
		postMail(String recipients[], String subject,
        String message , String from) {
			this.recipients=recipients;
			this.subject=subject;
			this.message=message;
			this.from=from;
		}
		
		public void run(){
		
			try {
				if (MiniTrucoServer.SERVER.get("SMTP_ENABLED").equals("TRUE")) {
					boolean debug = false;

					//Set the host smtp address
					Properties props = new Properties();
					props.put("mail.smtp.host", MiniTrucoServer.SERVER.get("SMTP_HOST_NAME"));
					props.put("mail.smtp.auth", "true");

					Authenticator auth = new SMTPAuthenticator();
					Session session = Session.getDefaultInstance(props, auth);

					session.setDebug(debug);
				
					// create a message
					Message msg = new MimeMessage(session);

					// set the from and to address
					InternetAddress addressFrom = new InternetAddress(from);
					msg.setFrom(addressFrom);

					InternetAddress[] addressTo = new InternetAddress[recipients.length];
					for (int i = 0; i < recipients.length; i++)
					{
						addressTo[i] = new InternetAddress(recipients[i]);
					}
					msg.setRecipients(Message.RecipientType.TO, addressTo);

					// Setting the Subject and Content Type
					msg.setSubject(subject);
					msg.setText(message);
					msg.setHeader("Content-Type", "text/plain; charset=UTF-8");
					Transport.send(msg);
				}
			} catch(Exception e) {
				//System.out.println(e.toString());
				ServerLogger.evento("Erro ao tentar enviar email");
			}
		}
    }


	/**
	 * SimpleAuthenticator is used to do simple authentication
	 * when the SMTP server requires it.
	 */
	private class SMTPAuthenticator extends javax.mail.Authenticator
	{

		public PasswordAuthentication getPasswordAuthentication()
		{
			String username = MiniTrucoServer.SERVER.get("SMTP_AUTH_USER");
			String password = MiniTrucoServer.SERVER.get("SMTP_AUTH_PWD");
			return new PasswordAuthentication(username, password);
		}
	}

}
