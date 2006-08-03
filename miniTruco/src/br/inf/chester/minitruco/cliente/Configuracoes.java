package br.inf.chester.minitruco.cliente;

/*
 * Copyright � 2006 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa � um software livre; voc� pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licen�a P�blica Geral GNU como 
 * publicada pela Funda��o do Software Livre (FSF); na vers�o 2 da 
 * Licen�a, ou (na sua opni�o) qualquer vers�o.
 *
 * Este programa � distribuido na esperan�a que possa ser util, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia implicita de ADEQUA��O
 * a qualquer MERCADO ou APLICA��O EM PARTICULAR. Veja a Licen�a
 * P�blica Geral GNU para maiores detalhes.
 *
 * Voc� deve ter recebido uma c�pia da Licen�a P�blica Geral GNU
 * junto com este programa, se n�o, escreva para a Funda��o do Software
 * Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 * Recupera e salva as op��es do usu�rio (regras, aspectos visuais,
 * nome do multiplayer, servidor default, etc.)
 * 
 * @author Chester
 * 
 */
public class Configuracoes {

	/**
	 * Marcador para saber quando n�o h� registro no recordstore.
	 * <p>
	 * A especifica��o MIDP diz que os n�meros come�am no 1, vou acreditar
	 */
	private static final int REGISTRO_INEXISTENTE = -1;

	/**
	 * Identificador do registro das configura��es no RecordStore
	 */
	private int recordId = REGISTRO_INEXISTENTE;

	/**
	 * Nome do jogador (usado no multiplayer)
	 */
	public String nomeJogador;

	/*
	 * Configura��es do menu op��es, nos seus valores default (vide MiniTruco
	 * para detalhes)
	 */
	public int nivel = 1;

	public boolean cartasGrandes;

	public boolean animacaoLigada = true;

	public boolean baralhoLimpo = false;

	public boolean manilhaVelha = false;

	public String servidor = Servidor.SERVIDOR_DEFAULT;

	/**
	 * Carrega as configura��es da mem�ria do celular
	 * 
	 * @return Objeto contendo as configura��es salvas (ou configura��es default
	 *         se houverem problemas)
	 */
	public static Configuracoes getConfiguracoes() {
		RecordStore rs = null;
		try {
			// As configura��es ficam num �nico record do RecordStore, basta
			// abrir, pegar o ID e trasnformar os dados
			rs = RecordStore.openRecordStore("miniTruco", true);
			Configuracoes c = new Configuracoes();
			RecordEnumeration recEnum = rs.enumerateRecords(null, null, false);
			if (recEnum.hasNextElement()) {
				// Guarda o ID (e usa ele pra puxar o registro)
				c.recordId = recEnum.nextRecordId();
				byte registro[] = rs.getRecord(c.recordId);
				// Decomp�e o registro em propriedades de configura��o
				DataInputStream disDados = new DataInputStream(
						new ByteArrayInputStream(registro));
				try {
					c.nivel = disDados.readInt();
					c.cartasGrandes = disDados.readBoolean();
					c.animacaoLigada = disDados.readBoolean();
					c.baralhoLimpo = disDados.readBoolean();
					c.manilhaVelha = disDados.readBoolean();
					c.nomeJogador = disDados.readUTF();
					c.servidor = disDados.readUTF();
				} catch (IOException e) {
					// Se der erro na leitura, retorna um objeto default, mas
					// com o ID deste (para que a pr�xima grava��o o
					// sobrescreva).
					Configuracoes cNovo = new Configuracoes();
					cNovo.recordId = c.recordId;
					return cNovo;
				}
			}
			rs.closeRecordStore();
			return c;
		} catch (RecordStoreException e) {
			// Se der erro, retorna um novo objeto
			Logger.debug(e.getMessage());
			return new Configuracoes();
		} finally {
			if (rs != null)
				try {
					rs.closeRecordStore();
				} catch (RecordStoreException re) {
					// Ai jisuis, por que um close declara exce��o?
					Logger.debug("Erro ao fechar rs:" + re.getMessage());
				}
		}

	}

	/**
	 * Salva as configura��es do objeto na mem�ria do celular.
	 * 
	 * @throws RecordStoreException
	 *             caso hajam problemas (para notificar o usu�rio que a
	 *             configura��o n�o foi salva)
	 */
	public void salva() {
		RecordStore rs = null;
		try {
			rs = RecordStore.openRecordStore("miniTruco", true);
			ByteArrayOutputStream baosDados = new ByteArrayOutputStream();
			DataOutputStream dosDados = new DataOutputStream(baosDados);

			// Comp�e o registro com as propriedades de configura��o
			dosDados.writeInt(nivel);
			dosDados.writeBoolean(cartasGrandes);
			dosDados.writeBoolean(animacaoLigada);
			dosDados.writeBoolean(baralhoLimpo);
			dosDados.writeBoolean(manilhaVelha);
			dosDados.writeUTF(nomeJogador == null ? "" : nomeJogador);
			dosDados.writeUTF(servidor == null ? "" : servidor);

			// Atualiza o recordstore
			byte[] dados = baosDados.toByteArray();
			if (this.recordId == REGISTRO_INEXISTENTE) {
				this.recordId = rs.addRecord(dados, 0, dados.length);
			} else {
				rs.setRecord(this.recordId, dados, 0, dados.length);
			}
			rs.closeRecordStore();
		} catch (RecordStoreException e) {
			// Se der erro, desencana (dificilmente o usu�rio poder� fazer algo
			// a respeito mesmo)
			Logger.debug(e.getMessage());
		} catch (IOException e) {
			// Idem
			Logger.debug(e.getMessage());
		} finally {
			if (rs != null)
				try {
					rs.closeRecordStore();
				} catch (RecordStoreException re) {
					// Ai jisuis, por que um close declara exce��o?
					Logger.debug("Erro ao fechar rs:" + re.getMessage());
				}
		}

	}

	/**
	 * Indica se as configura��es do objeto foram recuperadas do celular ou se
	 * s�o as default para primeira execu��o (ou para dispostivos que n�o
	 * suportam RecordStore)
	 * 
	 * @return true para configura��es default, false para configura��es
	 *         carregadas do celular.
	 */
	public boolean isDefault() {
		return this.recordId == REGISTRO_INEXISTENTE;
	}

}
