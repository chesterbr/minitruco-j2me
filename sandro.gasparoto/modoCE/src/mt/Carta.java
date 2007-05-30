package mt;

/*
 * Copyright � 2005-2007 Carlos Duarte do Nascimento (Chester)
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

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Representa uma carta do truco
 * 
 * @author Chester
 * 
 */
public class Carta {

	static {
		// Inicializa��o da classe
		setCartasGrandes(false);
	}

	/**
	 * Determina se as cartas s�o do modelo pequeno (13x20) ou grande (tbd)
	 */
	private static boolean cartasGrandes;

	/**
	 * Altura das cartas
	 */
	public static int alturaCartas;

	/**
	 * Largura das cartas
	 */
	public static int larguraCartas;

	/**
	 * Dist�ncia entre o topo da carta e o naipe
	 */
	private static int offset_topo_naipe;

	/**
	 * Dist�ncia entre o topo da carta e o valor
	 */
	private static int offset_topo_valor;

	private static int offset_esquerda_naipe;

	/**
	 * Imagens cacheadas dos quatro naipes normais, seguidas deles "escurecidos"
	 */
	private static Image[] naipes;

	/**
	 * Imagens cacheadas dos 13 valores (A, 2, 3, ..., K), seguidas dos 13
	 * valores "escurecidos"
	 */
	private static Image[][] valores;

	/**
	 * Imagem cacheada do fundo da carta (quando n�o est� virada)
	 */
	private static Image fundoCarta;

	/**
	 * Constante que representa o naipe de copas
	 */
	public static final int NAIPE_COPAS = 0;

	/**
	 * Constante que representa o naipe de ouros
	 */
	public static final int NAIPE_OUROS = 1;

	/**
	 * Constante que representa o naipe de espadas
	 */
	public static final int NAIPE_ESPADAS = 2;

	/**
	 * Constante que representa o naipe de paus
	 */
	public static final int NAIPE_PAUS = 3;

	/**
	 * Lista ordenada dos naipes
	 */
	public static final int[] NAIPES = { NAIPE_COPAS, NAIPE_ESPADAS,
			NAIPE_OUROS, NAIPE_PAUS };

	/**
	 * Indica que o naipe da carta n�o foi escolhido
	 */
	public static final int NAIPE_NENHUM = 4;

	/**
	 * Indica que a letra da carta n�o foi escolhida
	 */
	public static final char LETRA_NENHUMA = 'X';

	private static final String LETRAS_VALIDAS = "A23456789JQK";

	private boolean cartaEmJogo = true;

	private char letra = LETRA_NENHUMA;

	private int naipe = NAIPE_NENHUM;

	private int top;

	private int left;

	private boolean virada = false;

	private boolean fechada = false;

	/**
	 * Cria uma carta com letra e naipe definidos, j� "virada" (vis�vel)
	 * 
	 * @param letra
	 * @param naipe
	 */
	public Carta(char letra, int naipe) {
		setLetra(letra);
		setNaipe(naipe);
		setVirada(true);
	}

	/**
	 * Cria uma carta baseado em sua representa��o string
	 * 
	 * @param sCarta
	 *            letra e naipe da carta, conforme retornado por <code>toString()</code>
	 * @see Carta#toString()
	 */
	public Carta(String sCarta) {
		this(sCarta.charAt(0), "coepx".indexOf(sCarta.charAt(1)));
	}

	/**
	 * Cria uma carta vazia na posi��o especificada
	 * <p>
	 * Obs.: a carta aparece desvirada, e n�o pode ser virada enquanto n�o forem
	 * atribu�dos naipe e letra
	 */
	public Carta(int left, int top) {
		setTop(top);
		setLeft(left);
	}

	/**
	 * Cria uma carta vazia, posicionada em 0,0
	 * 
	 * @see Carta#Carta(int, int)
	 * 
	 */
	public Carta() {

	}

	/**
	 * Muda o tamanho das cartas.
	 * <p>
	 * � importante dar um repaint na mesa depois de setar isto
	 * 
	 * @param isCartasGrandes
	 *            true para cartas grandes, false para pequenas
	 */
	public static void setCartasGrandes(boolean isCartasGrandes) {

		cartasGrandes = isCartasGrandes;

		// Seta os novos valores de altura/largura
		if (cartasGrandes) {
			larguraCartas = 23;
			alturaCartas = 36;
			offset_topo_valor = 2;
			offset_topo_naipe = 18;
			offset_esquerda_naipe = 4;
		} else {
			larguraCartas = 13;
			alturaCartas = 20;
			offset_topo_valor = 2;
			offset_topo_naipe = 10;
			offset_esquerda_naipe = 3;
		}

		// Limpa os bitmaps cacheados
		naipes = null;
		valores = null;
		fundoCarta = null;

	}

	public static boolean isCartasGrandes() {
		return cartasGrandes;
	}

	/**
	 * Determina a letra (valor facial) da carta.
	 * <p>
	 * Letras v�lidas s�o as da constante LETRAS_VALIDAS. Se a letra for
	 * inv�lida, a propriedade n�o � alterda.
	 * 
	 * @param letra
	 */
	public void setLetra(char letra) {
		if (LETRAS_VALIDAS.indexOf(letra) != -1 || letra == LETRA_NENHUMA) {
			this.letra = letra;
		}
	}

	public char getLetra() {
		return letra;
	}

	/**
	 * Seta o naipe da carta.
	 * <p>
	 * Caso o naipe seja inv�lido, n�o � alterado
	 * 
	 * @param naipe
	 *            Naipe de acordo com as constantes
	 */
	public void setNaipe(int naipe) {
		if (naipe == NAIPE_COPAS || naipe == NAIPE_OUROS || naipe == NAIPE_PAUS
				|| naipe == NAIPE_ESPADAS || naipe == NAIPE_NENHUM) {
			this.naipe = naipe;
		}
	}

	public int getNaipe() {
		return naipe;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public int getTop() {
		return top;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getLeft() {
		return left;
	}

	public int getValor() {
		return LETRAS_VALIDAS.indexOf(letra);
	}

	/**
	 * Determina se uma carta est� virdada (mostrando o valor) ou n�o (mostrando
	 * o desenho).
	 * <p>
	 * Obs.: cartas t�m que ter uma letra e naipe para serem viradas, e cartas
	 * fechadas n�o podem ser viradas
	 * 
	 * @param virada
	 */
	public void setVirada(boolean virada) {
		this.virada = virada && (!fechada) && (letra != LETRA_NENHUMA)
				&& (naipe != NAIPE_NENHUM);
	}

	/**
	 * Determina se uma carta est� virada (mostrando o valor)
	 * 
	 * @return True se a carta est� mostrando o valor, false caso contr�rio
	 */
	public boolean isVirada() {
		return virada;
	}

	/**
	 * Determina que uma carta foi jogada como "fechada", e seu valor deve ser
	 * ignorado.
	 * 
	 * @param fechada
	 */
	public void setFechada(boolean fechada) {
		this.fechada = fechada;
		// Se uma carta for fechada, n�o pode estar virada (vis�vel)
		if (fechada)
			this.virada = false;
	}

	public boolean isFechada() {
		return fechada;
	}

	/**
	 * Recupera o bitmap corresponente ao naipe da carta
	 * 
	 * @return
	 */
	private synchronized Image getImgNaipe() {
		// Inicializa, se necessario, os bitmaps dos naipes
		if (naipes == null) {
			try {
				int larguraNaipe = (cartasGrandes ? 15 : 7);
				Image imgNaipes = Image
						.createImage(cartasGrandes ? "/naipes_g.png"
								: "/naipes.png");
				Image imgNaipesEscuro = Image
						.createImage(cartasGrandes ? "/naipes_g_escuro.png"
								: "/naipes_escuro.png");
				naipes = new Image[8];
				for (int i = 0; i < 4; i++) {
					naipes[i] = Image.createImage(larguraNaipe, larguraNaipe);
					naipes[i].getGraphics().drawImage(imgNaipes,
							-larguraNaipe * i, 0, Graphics.TOP | Graphics.LEFT);
					naipes[i + 4] = Image.createImage(larguraNaipe,
							larguraNaipe);
					naipes[i + 4].getGraphics().drawImage(imgNaipesEscuro,
							-larguraNaipe * i, 0, Graphics.TOP | Graphics.LEFT);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Recupera o bitmap apropriado
		return naipes[getNaipe() + (cartaEmJogo ? 0 : 4)];
	}

	/**
	 * Larguras dos bitmaps recortados dos valores para as cartas grandes (as
	 * pequenas s�o monoespa�adas, e eu n�o tive saco de fazer isso acontecer
	 * nas grandes via editor gr�fico)
	 */
	private static final int[] largurasCartasGrandes = { 13, 12, 11, 12, 11,
			12, 11, 12, 11, 12, 12, 14 };

	/**
	 * Recupera o bitmap corresponente ao valor da carta
	 * 
	 * @return
	 */
	private synchronized Image getImgValor() {
		// Inicializa, se necessario, os bitmaps dos valores
		// (s�o 13 para os naipes vermelhos e 13 para os naipes pretos)
		if (valores == null) {
			try {
				Image imgValores = Image
						.createImage(cartasGrandes ? "/valores_g.png"
								: "/valores.png");
				Image imgValoresEscuros = Image
						.createImage(cartasGrandes ? "/valores_g_escuro.png"
								: "/valores_escuro.png");
				valores = new Image[24][2];
				for (int i = 0; i <= 11; i++) {
					// Determina onde e com que largura recortar o valor
					int alturaValor, larguraValor, posValor;
					if (cartasGrandes) {
						alturaValor = 17;
						larguraValor = largurasCartasGrandes[i];
						// "Volta" os valores correspondentes �s cartas
						// anteriores
						posValor = 0;
						for (int j = 0; j < i; j++)
							posValor -= largurasCartasGrandes[j];
					} else {
						alturaValor = 7;
						larguraValor = 4;
						posValor = -5 * i;
					}
					// Recora o vermelho e o preto para este valor
					valores[i][0] = Image
							.createImage(larguraValor, alturaValor);
					valores[i][0].getGraphics().drawImage(imgValores, posValor,
							0, Graphics.TOP | Graphics.LEFT);
					valores[i][1] = Image
							.createImage(larguraValor, alturaValor);
					valores[i][1].getGraphics().drawImage(imgValores, posValor,
							-alturaValor, Graphics.TOP | Graphics.LEFT);
					// Faz o mesmo recorte para a vers�o "escura"
					valores[i + 12][0] = Image.createImage(larguraValor,
							alturaValor);
					valores[i + 12][0].getGraphics().drawImage(
							imgValoresEscuros, posValor, 0,
							Graphics.TOP | Graphics.LEFT);
					valores[i + 12][1] = Image.createImage(larguraValor,
							alturaValor);
					valores[i + 12][1].getGraphics().drawImage(
							imgValoresEscuros, posValor, -alturaValor,
							Graphics.TOP | Graphics.LEFT);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Recupera o bitmap apropriado (usando o naipe para determinar a cor)
		return valores[getValor() + (cartaEmJogo ? 0 : 12)][(naipe == NAIPE_ESPADAS)
				|| (naipe == NAIPE_PAUS) ? 0 : 1];
	}

	private synchronized Image getImgFundoCarta() {
		if (fundoCarta == null) {
			try {
				fundoCarta = Image
						.createImage(cartasGrandes ? "/fundocarta_g.png"
								: "/fundocarta.png");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fundoCarta;
	}

	/**
	 * Mostra que a carta em quest�o � a carta selecionada pelo usu�rio
	 * 
	 * @param g
	 *            Local onde ser� desenhada a marca de sele��o
	 */
	public void desenhaCursor(Graphics g) {
		// Borda vermelha
		g.setColor(0x00CC0000);
		g.drawRect(left, top, larguraCartas - 1, alturaCartas - 1);
		g.drawRect(left - 1, top - 1, larguraCartas + 1, alturaCartas + 1);
	}

	public void destacaVitoriosa(Graphics g) {
		g.setColor(0x000000FF);
		g.drawRect(left, top, larguraCartas - 1, alturaCartas - 1);
		g.drawRect(left - 1, top - 1, larguraCartas + 1, alturaCartas + 1);
	}

	/**
	 * Renderiza a carta
	 * 
	 * @param g
	 *            Local onde a carta ser� desenhada
	 */
	public void desenhaCarta(Graphics g) {

		g.setColor(0x00000000);
		g.drawLine(left + 1, top, left + larguraCartas - 2, top);
		g.drawLine(left + 1, top + alturaCartas - 1, left + larguraCartas - 2,
				top + alturaCartas - 1);
		g.drawLine(left, top + 1, left, top + alturaCartas - 2);
		g.drawLine(left + larguraCartas - 1, top, left + larguraCartas - 1, top
				+ alturaCartas - 2);
		if (virada) {
			if (cartaEmJogo) {
				g.setColor(0x00FFFFFF);
			} else {
				g.setColor(0x00CCCCCC);
			}
			g.fillRect(left + 1, top + 1, larguraCartas - 2, alturaCartas - 2);
			g.drawImage(getImgNaipe(), left + offset_esquerda_naipe, top
					+ offset_topo_naipe, Graphics.TOP | Graphics.LEFT);
			g.drawImage(getImgValor(), left + 3, top + offset_topo_valor,
					Graphics.TOP | Graphics.LEFT);
		} else {
			g.drawImage(getImgFundoCarta(), left + 1, top + 1, Graphics.TOP
					| Graphics.LEFT);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object outroObjeto) {
		if ((outroObjeto != null) && (outroObjeto instanceof Carta)) {
			Carta outraCarta = (Carta) outroObjeto;
			return outraCarta.getNaipe() == this.getNaipe()
					&& outraCarta.getLetra() == this.getLetra();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getLetra() * 256 + getNaipe();
	}

	/**
	 * Retorna um valor de 1 a 14 para esta carta, considerando a manilha
	 * 
	 * @param letraManilha
	 *            letra da manilha desta rodada
	 * @return valor que permite comparar duas cartas
	 */
	public int getValorTruco(char letraManilha) {
		return Jogo.getValorTruco(this, letraManilha);
	}

	/**
	 * Retorna true se um clique do apontador nesta posi��o corresponde a esta
	 * carta
	 * 
	 * @param x
	 *            posi��o horizontal na tela
	 * @param y
	 *            posi��o vertical na tela
	 * @return true se a carta � atingida pelo clique (n�o considerando z-order)
	 */
	public boolean isClicado(int x, int y) {
		return (x >= left) && (y >= top) && (x <= left + larguraCartas)
				&& (y <= top + alturaCartas);
	}

	/**
	 * Representa��o em 2 caracteres da carta, formada por letra (em
	 * "A234567QJK") e naipe ([c]opas, [o]uro, [e]spadas,[p]aus ou [x] para
	 * nenhum).
	 * <p>
	 * Esta representa��o � usada na comunica��o cliente-servidor, ent�o n�o
	 * deve ser alterada (ou, se for, o construtor baseado em caractere deve ser
	 * alterado de acordo).
	 */
	public String toString() {
		return letra + "" + ("coepx").charAt(naipe);
	}

	/**
	 * Escurece/clareia uma carta para indicar que ela n�o est�/est� em jogo
	 * 
	 * @param cartaEmJogo
	 *            true para clarear, false para escurecer
	 */
	public void setCartaEmJogo(boolean cartaEmJogo) {
		this.cartaEmJogo = cartaEmJogo;
	}

	/**
	 * Indica se a carta est� em jogo, e, portanto, deve ficar "clarinha" (as
	 * cartas de rodadas passadas s�o escurecidas
	 */
	public boolean isCartaEmJogo() {
		return cartaEmJogo;
	}

}
