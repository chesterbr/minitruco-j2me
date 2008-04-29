package mt;

/*
 * Copyright © 2005-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Copyright © 2007 Sandro Gasparotto (sandro.gasparoto@gmail.com)
 * (modo confronto de estratégias)
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
	private static final int VERSAO_ID = 109;
	private static final int VERSAO_ID_MODOCE = 10112;

	/**
	 * Identificador do registro das configurações no RecordStore
	 */
	private int recordId = REGISTRO_INEXISTENTE;

	/*
	 * Configurações do menu opções, nos seus valores default. <p> A classe
	 * <code>miniTruco</code> interpreta estes valores.
	 */
	public int[] estrategias = { Jogador.opcoesEstrategia.length - 1,
			Jogador.opcoesEstrategia.length - 1,
			Jogador.opcoesEstrategia.length - 1 };

	public int[] estrategiasModoCE = { Jogador.opcoesEstrategia.length - 1,
			Jogador.opcoesEstrategia.length - 1 };

	public boolean cartasGrandes = isTelaGrande();

	public boolean animacaoLigada = true;

	public boolean baralhoLimpo = false;

	public boolean manilhaVelha = false;

	public int nPartidasModoCE = 1;

	public String servidor = SERVIDOR_DEFAULT;

	public String idioma = null;

	public String nomeJogador;

	public static final String PORTA_DEFAULT = "6912";

	public static final String SERVIDOR_DEFAULT = "chester.servegame.org:"
			+ PORTA_DEFAULT;

	/**
	 * Cria uma instância com os valores default (incluindo auto-detecção de
	 * idioma)
	 */
	private Configuracoes() {
		idioma = "Português";

		// [IF_FULL]
		String locale = null;
		try {
			locale = System.getProperty("microedition.locale");
		} catch (Exception e) {
			// Se der erro, provavelmente estamos rodando do servidor.
			// Define o default pela presença ou não do arquivo
			if (this.getClass().getResourceAsStream("/forceEnglishOnServer") != null) {
				idioma = "English";
			}
		}
		if (locale != null && locale.toLowerCase().startsWith("en")) {
			idioma = "English";
		}
		// [ENDIF_FULL]
	}

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
					c.estrategias[0] = disDados.readInt();
					c.estrategias[1] = disDados.readInt();
					c.estrategias[2] = disDados.readInt();
					c.estrategiasModoCE[0] = disDados.readInt();
					c.estrategiasModoCE[1] = disDados.readInt();
					c.cartasGrandes = disDados.readBoolean();
					c.animacaoLigada = disDados.readBoolean();
					c.baralhoLimpo = disDados.readBoolean();
					c.manilhaVelha = disDados.readBoolean();
					c.nomeJogador = disDados.readUTF();
					c.servidor = disDados.readUTF();
					c.idioma = disDados.readUTF();
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
			Jogo.log(e.getMessage());
			return new Configuracoes();
		} finally {
			if (rs != null)
				try {
					rs.closeRecordStore();
				} catch (RecordStoreException re) {
					// Ai jisuis, por que um close declara exceção?
					// Jogo.log("Erro ao fechar rs:" + re.getMessage());
				}
		}

	}

	/**
	 * Carrega as configurações do modo confronto de estratégias da memória do
	 * celular Criei este método somente para o modo CE de forma a deixá-lo mais
	 * independente do resto do código, pois poderemos implementar dados
	 * exclusivos para este modo no futuro...
	 * 
	 * @return Objeto contendo as configurações salvas (ou configurações default
	 *         se houverem problemas)
	 */
	public static Configuracoes getConfiguracoesModoCE() {
		RecordStore rs = null;
		try {
			// As configurações ficam num único record do RecordStore, basta
			// abrir, pegar o ID e trasnformar os dados
			rs = RecordStore.openRecordStore("miniTrucoModoCE", true);
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
					if (disDados.readInt() != VERSAO_ID_MODOCE) {
						// Configuração velha, manda pro lixo
						throw new IOException();
					}
					c.estrategiasModoCE[0] = disDados.readInt();
					c.estrategiasModoCE[1] = disDados.readInt();
					c.nPartidasModoCE = disDados.readInt();
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
			Jogo.log(e.getMessage());
			return new Configuracoes();
		} finally {
			if (rs != null)
				try {
					rs.closeRecordStore();
				} catch (RecordStoreException re) {
					// Ai jisuis, por que um close declara exceção?
					Jogo.log("Erro ao fechar rs:" + re.getMessage());
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
			dosDados.writeInt(estrategias[0]);
			dosDados.writeInt(estrategias[1]);
			dosDados.writeInt(estrategias[2]);
			dosDados.writeInt(estrategiasModoCE[0]);
			dosDados.writeInt(estrategiasModoCE[1]);
			dosDados.writeBoolean(cartasGrandes);
			dosDados.writeBoolean(animacaoLigada);
			dosDados.writeBoolean(baralhoLimpo);
			dosDados.writeBoolean(manilhaVelha);
			dosDados.writeUTF(nomeJogador == null ? "" : nomeJogador);
			dosDados.writeUTF(servidor == null ? "" : servidor);
			dosDados.writeUTF(idioma == null ? "" : idioma);

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
			Jogo.log(e.getMessage());
		} catch (IOException e) {
			// Idem
			Jogo.log(e.getMessage());
		} finally {
			if (rs != null)
				try {
					rs.closeRecordStore();
				} catch (RecordStoreException re) {
					// Ai jisuis, por que um close declara exceção?
					Jogo.log("Erro ao fechar rs:" + re.getMessage());
				}
		}

	}

	/**
	 * Salva as configurações do objeto na memória do celular. Criei este método
	 * somente para o modo CR de forma a deixá-lo mais independente do resto do
	 * código, pois poderemos implementar dados exclusivos para este modo no
	 * futuro...
	 * 
	 * @throws RecordStoreException
	 *             caso hajam problemas (para notificar o usuário que a
	 *             configuração não foi salva)
	 */
	public void salvaModoCE() {
		RecordStore rs = null;
		try {
			rs = RecordStore.openRecordStore("miniTrucoModoCE", true);
			ByteArrayOutputStream baosDados = new ByteArrayOutputStream();
			DataOutputStream dosDados = new DataOutputStream(baosDados);

			// Compõe o registro com as propriedades de configuração
			dosDados.writeInt(VERSAO_ID_MODOCE);
			dosDados.writeInt(estrategiasModoCE[0]);
			dosDados.writeInt(estrategiasModoCE[1]);
			dosDados.writeInt(nPartidasModoCE);

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
			Jogo.log(e.getMessage());
		} catch (IOException e) {
			// Idem
			Jogo.log(e.getMessage());
		} finally {
			if (rs != null)
				try {
					rs.closeRecordStore();
				} catch (RecordStoreException re) {
					// Ai jisuis, por que um close declara exceção?
					Jogo.log("Erro ao fechar rs:" + re.getMessage());
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
