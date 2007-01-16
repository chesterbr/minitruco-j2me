package mt;

/*
 * Copyright © 2006 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa é um software livre; você pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 2 da 
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 * Recupera e salva as opções do usuário (regras, aspectos visuais, nome do
 * multiplayer, servidor default, etc.)
 * 
 * @author Chester
 * 
 */
public class Configuracoes {

	/**
	 * Marcador para saber quando não há registro no recordstore.
	 * <p>
	 * A especificação MIDP diz que os números começam no 1, vou acreditar
	 */
	private static final int REGISTRO_INEXISTENTE = -1;

	/**
	 * Identifica a versão da configuração. Quando novas versões do miniTruco
	 * quebrarem a compatibilidade, basta incrementar este número para garantir
	 * que antigos registros sejam descartados
	 */
	private static final int VERSAO_ID = 104;

	/**
	 * Identificador do registro das configurações no RecordStore
	 */
	private int recordId = REGISTRO_INEXISTENTE;

	/*
	 * Configurações do menu opções, nos seus valores default (vide MiniTruco
	 * para detalhes)
	 */
	public String[] estrategias = { "Sortear", "Sortear", "Sortear" };

	public boolean cartasGrandes = isTelaGrande();

	public boolean animacaoLigada = true;

	public boolean baralhoLimpo = false;

	public boolean manilhaVelha = false;

	/**
	 * Carrega as configurações da memória do celular
	 * 
	 * @return Objeto contendo as configurações salvas (ou configurações default
	 *         se houverem problemas)
	 */
	public static Configuracoes getConfiguracoes() {
		RecordStore rs = null;
		try {
			// As configurações ficam num único record do RecordStore, basta
			// abrir, pegar o ID e trasnformar os dados
			rs = RecordStore.openRecordStore("miniTruco", true);
			Configuracoes c = new Configuracoes();
			RecordEnumeration recEnum = rs.enumerateRecords(null, null, false);
			if (recEnum.hasNextElement()) {
				// Guarda o ID (e usa ele pra puxar o registro)
				c.recordId = recEnum.nextRecordId();
				byte registro[] = rs.getRecord(c.recordId);
				// Decompõe o registro em propriedades de configuração
				DataInputStream disDados = new DataInputStream(
						new ByteArrayInputStream(registro));
				try {
					if (disDados.readInt() != VERSAO_ID) {
						// Configuração velha, manda pro lixo
						throw new IOException();
					}
					c.estrategias[0] = disDados.readUTF();
					c.estrategias[1] = disDados.readUTF();
					c.estrategias[2] = disDados.readUTF();
					c.cartasGrandes = disDados.readBoolean();
					c.animacaoLigada = disDados.readBoolean();
					c.baralhoLimpo = disDados.readBoolean();
					c.manilhaVelha = disDados.readBoolean();
				} catch (IOException e) {
					// Se der erro na leitura, retorna um objeto default, mas
					// com o ID deste (para que a próxima gravação o
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
					// Ai jisuis, por que um close declara exceção?
					Logger.debug("Erro ao fechar rs:" + re.getMessage());
				}
		}

	}

	/**
	 * Salva as configurações do objeto na memória do celular.
	 * 
	 * @throws RecordStoreException
	 *             caso hajam problemas (para notificar o usuário que a
	 *             configuração não foi salva)
	 */
	public void salva() {
		RecordStore rs = null;
		try {
			rs = RecordStore.openRecordStore("miniTruco", true);
			ByteArrayOutputStream baosDados = new ByteArrayOutputStream();
			DataOutputStream dosDados = new DataOutputStream(baosDados);

			// Compõe o registro com as propriedades de configuração
			dosDados.writeInt(VERSAO_ID);
			dosDados.writeUTF(estrategias[0]);
			dosDados.writeUTF(estrategias[1]);
			dosDados.writeUTF(estrategias[2]);
			dosDados.writeBoolean(cartasGrandes);
			dosDados.writeBoolean(animacaoLigada);
			dosDados.writeBoolean(baralhoLimpo);
			dosDados.writeBoolean(manilhaVelha);

			// Atualiza o recordstore
			byte[] dados = baosDados.toByteArray();
			if (this.recordId == REGISTRO_INEXISTENTE) {
				this.recordId = rs.addRecord(dados, 0, dados.length);
			} else {
				rs.setRecord(this.recordId, dados, 0, dados.length);
			}
			rs.closeRecordStore();
		} catch (RecordStoreException e) {
			// Se der erro, desencana (dificilmente o usuário poderá fazer algo
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
					// Ai jisuis, por que um close declara exceção?
					Logger.debug("Erro ao fechar rs:" + re.getMessage());
				}
		}

	}

	/**
	 * Indica se as configurações do objeto foram recuperadas do celular ou se
	 * são as default para primeira execução (ou para dispostivos que não
	 * suportam RecordStore)
	 * 
	 * @return true para configurações default, false para configurações
	 *         carregadas do celular.
	 */
	public boolean isDefault() {
		return this.recordId == REGISTRO_INEXISTENTE;
	}

	private static Boolean telaGrande = null;

	/**
	 * Determina se este celular tem uma tela suficientemente grande para
	 * mostrar as "cartas grandes"
	 * 
	 * @return
	 */
	private static boolean isTelaGrande() {
		// Classe dummy usada para medir a tela
		class CanvasParaMedir extends Canvas {
			protected void paint(Graphics arg0) {
			}
		}
		// Se ainda não fez, mede a tela e toma a decisão (fiz assim por não
		// saber o custo de instanciar um Canvas a cada chamada)
		if (telaGrande == null) {
			Canvas c = new CanvasParaMedir();
			telaGrande = new Boolean(c.getWidth() >= 99 && c.getHeight() >= 140);
		}
		return telaGrande.booleanValue();
		}

}
