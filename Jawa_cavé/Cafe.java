// -*- coding: utf-8 -*-
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Cafe {

    // ── Constantes ────────────────────────────────────────────────────────────
    static final String ARQUIVO_CSV = "escala_cafe.csv";

    static final String[] INSTRUCOES = {
        "1  Desligue a garrafa de café",
        "2  Retire o pó usado e o café que sobrou do dia anterior",
        "3  Lave o coador",
        "4  Complete com água filtrada até a marca MAX (6L) da garrafa",
        "5  Insira o pino e o coador novamente na garrafa",
        "6  Coloque 16 colheres cheias de pó de café no coador (para 6L)",
        "7  Feche e ligue a cafeteira",
        "8  Informe o horário que ficará pronto (~30 min)",
    };

    static final String MARCAS       = "3 Corações (Forte, Extra Forte, Tradicional)  •  Melita";
    static final String INGREDIENTES = "Pó de café · 16 medidas · Água 6L (MAX) · ~30 min";

    // ── Helpers ────────────────────────────────────────────────────────────────
    static final String[] DIAS_PT  = {"Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"};
    static final String[] MESES_PT = {"jan","fev","mar","abr","mai","jun","jul","ago","set","out","nov","dez"};

    static final DateTimeFormatter FMT_FULL  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    static final DateTimeFormatter FMT_SHORT = DateTimeFormatter.ofPattern("dd/MM/yy");

    // ── Carregamento da escala ─────────────────────────────────────────────────
    /**
     * Lê escala_cafe.csv e retorna lista de Object[]{nome, data, pacotes}.
     * O CSV deve ter cabeçalho: nome,data,pacotes
     * Linhas em branco ou com nome vazio são ignoradas.
     */
    static List<Object[]> carregarEscala() throws IOException {
        List<Object[]> lista = new ArrayList<>();
        File arquivo = new File(ARQUIVO_CSV);
        if (!arquivo.exists()) {
            throw new IOException("Arquivo não encontrado: " + ARQUIVO_CSV +
                "\nExecute o cafe.bat para converter a planilha automaticamente.");
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {
            String cabecalho = br.readLine(); // pula cabeçalho
            if (cabecalho == null) return lista;
            String linha;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;
                String[] partes = linha.split(",", -1);
                if (partes.length < 3) continue;
                String nome = partes[0].trim();
                String data = partes[1].trim();
                if (nome.isEmpty() || data.isEmpty()) continue;
                int pacotes = 0;
                try { pacotes = Integer.parseInt(partes[2].trim()); }
                catch (NumberFormatException ignored) {}
                lista.add(new Object[]{nome, data, pacotes});
            }
        }
        return lista;
    }

    // ── Formatação ─────────────────────────────────────────────────────────────
    static void limpar() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static String repetir(String s, int n) {
        StringBuilder sb = new StringBuilder(n * s.length());
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }

    static String linha(char ch, int n) {
        return repetir(String.valueOf(ch), n);
    }

    static String centralizar(String texto, int largura) {
        int pad = Math.max((largura - texto.length()) / 2, 0);
        return repetir(" ", pad) + texto;
    }

    static LocalDate parseData(String s) {
        for (DateTimeFormatter fmt : new DateTimeFormatter[]{FMT_FULL, FMT_SHORT}) {
            try { return LocalDate.parse(s, fmt); } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    static String formatarData(LocalDate dt) {
        int dow = dt.getDayOfWeek().getValue() - 1;
        return String.format("%02d/%s/%d  (%s)",
            dt.getDayOfMonth(),
            MESES_PT[dt.getMonthValue() - 1],
            dt.getYear(),
            DIAS_PT[dow]);
    }

    static String pacotesLabel(int n) {
        if (n == 0) return "0 pacotes doados :(";
        if (n == 1) return "1 pacote doado";
        return n + " pacotes doados";
    }

    // ── ASCII Art ──────────────────────────────────────────────────────────────
    static final String HEADER =
        "\n" +
        "                        (\n" +
        "                          )     (\n" +
        "                   ___...(-------)-....___\n" +
        "               .-\"\"       )    (          \"\"-.\n" +
        "         .-'``'|-._             )         _.-|\n" +
        "        /  .--.|   `\"\"---...........---\"\"`   |\n" +
        "       /  /    |                             |\n" +
        "       |  |    |                             |\n" +
        "        \\  \\   |           Bom dia!          |\n" +
        "         `\\ `\\ |             FSFX            |\n" +
        "           `\\ `|                             |\n" +
        "           _/ /\\                             /\n" +
        "          (__/  \\                           /\n" +
        "       _..---\"\"` \\                         /`\"\"---.._\n" +
        "    .-'           \\                       /          '-.\n" +
        "   :               `-.__             __.-'              :\n" +
        "   :                  ) \"\"---...---\"\" (                 :\n" +
        "    '._               `\"--...___...--\"`              _.'\n" +
        "      \\\"\"--..__ (                           ) __..--\"\"/\n" +
        "       '._     \"\"\"----.....______.....----\"\"\"     _.'\n" +
        "          `\"\"--..,,_____            _____,,..--\"\"`\n" +
        "                        `\"\"\"----\"\"\"`\n";

    static final String[] TITULO = {
        "╔══════════════════════════════════════════════════════════════════╗",
        "║      ☕  ESCALA DE CAFÉ  —  FUNDAÇÃO SÃO FRANCISCO XAVIER  ☕   ║",
        "║                    REV. 1  ·  03/26 - 06/26                      ║",
        "╚══════════════════════════════════════════════════════════════════╝",
    };

    static void imprimirAscii(String texto) {
        String[] linhas = texto.split("\n", -1);
        int maxLen = 0;
        for (String l : linhas) if (l.trim().length() > maxLen) maxLen = l.trim().length();
        int offset = Math.max((68 - maxLen) / 2, 0);
        for (String l : linhas) System.out.println(repetir(" ", offset) + l);
    }

    static void imprimirCabecalho() {
        limpar();
        imprimirAscii(HEADER);
        for (String t : TITULO) System.out.println(centralizar(t, 68));
        System.out.println();
    }

    static void imprimirInstrucoes() {
        System.out.println(centralizar("┌─────────────────────────────────────┐", 68));
        System.out.println(centralizar("│   PROCEDIMENTO PARA FAZER O CAFÉ    │", 68));
        System.out.println(centralizar("└─────────────────────────────────────┘", 68));
        System.out.println();
        for (String passo : INSTRUCOES) System.out.println("    " + passo);
        System.out.println();
        System.out.println("  Marcas    : " + MARCAS);
        System.out.println("  Info      : " + INGREDIENTES);
        System.out.println();
        System.out.println(linha('─', 68));
    }

    // ── Busca ──────────────────────────────────────────────────────────────────
    static void buscarPorNome(List<Object[]> escala, String termo) {
        termo = termo.trim().toLowerCase();
        List<Object[]> resultados = new ArrayList<>();
        for (Object[] entrada : escala) {
            if (((String) entrada[0]).toLowerCase().contains(termo))
                resultados.add(entrada);
        }
        if (resultados.isEmpty()) {
            System.out.printf("%n  ✗  Nenhum resultado para \"%s\".%n%n", termo);
            return;
        }
        System.out.printf("%n  ✔  %d entrada(s) encontrada(s):%n%n", resultados.size());
        System.out.printf("  %-22s %-30s %s%n", "NOME", "DATA", "PACOTES");
        System.out.println("  " + linha('-', 60));
        for (Object[] e : resultados) {
            LocalDate dt   = parseData((String) e[1]);
            String dataFmt = dt != null ? formatarData(dt) : (String) e[1];
            System.out.printf("  %-22s %-30s %s%n", e[0], dataFmt, pacotesLabel((int) e[2]));
        }
        System.out.println();
    }

    static void buscarPorData(List<Object[]> escala, String entrada) {
        entrada = entrada.trim();
        LocalDate dtBusca = parseData(entrada);
        if (dtBusca == null) {
            try {
                dtBusca = LocalDate.parse(entrada + "/2026", FMT_FULL);
            } catch (DateTimeParseException e) {
                System.out.println("\n  ✗  Data inválida. Use o formato DD/MM ou DD/MM/AAAA.\n");
                return;
            }
        }

        int dow = dtBusca.getDayOfWeek().getValue();
        if (dow >= 6) {
            String nomeDia = dow == 6 ? "SÁBADO" : "DOMINGO";
            System.out.printf("%n  ☕  Hoje é %s... café só na casa de mamãe! 🏠%n%n", nomeDia);
            return;
        }

        String dataFmt = formatarData(dtBusca);
        List<Object[]> encontrados = new ArrayList<>();
        for (Object[] e : escala) {
            LocalDate dt = parseData((String) e[1]);
            if (dt != null && dt.equals(dtBusca))
                encontrados.add(e);
        }

        if (encontrados.isEmpty()) {
            System.out.printf("%n  ✗  Nenhum responsável cadastrado para %s.%n%n", dataFmt);
            return;
        }

        System.out.printf("%n  📅  %s%n%n", dataFmt);
        System.out.printf("  %-25s %s%n", "RESPONSÁVEL", "PACOTES");
        System.out.println("  " + linha('-', 45));
        for (Object[] e : encontrados)
            System.out.printf("  %-25s %s%n", e[0], pacotesLabel((int) e[2]));
        System.out.println();
    }

    // ── Menu ───────────────────────────────────────────────────────────────────
    static void menu(int totalRegistros) {
        imprimirCabecalho();
        imprimirInstrucoes();
        System.out.printf("  Escala carregada: %d responsável(is)  ←  %s%n%n",
            totalRegistros, ARQUIVO_CSV);
        System.out.println("  PESQUISA");
        System.out.println("  " + linha('-', 40));
        System.out.println("  [1]  Buscar por NOME");
        System.out.println("  [2]  Buscar por DATA  (DD/MM ou DD/MM/AAAA)");
        System.out.println("  [0]  Sair");
        System.out.println();
    }

    // ── Main ───────────────────────────────────────────────────────────────────
    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        Scanner sc = new Scanner(System.in, "UTF-8");

        List<Object[]> escala;
        try {
            escala = carregarEscala();
        } catch (IOException ex) {
            System.out.println("\n  ERRO: " + ex.getMessage() + "\n");
            System.out.print("  Pressione ENTER para sair...");
            sc.nextLine();
            return;
        }

        if (escala.isEmpty()) {
            System.out.println("\n  AVISO: Nenhum dado encontrado em " + ARQUIVO_CSV + ".\n");
            System.out.print("  Pressione ENTER para sair...");
            sc.nextLine();
            return;
        }

        while (true) {
            menu(escala.size());
            System.out.print("  Opção: ");
            String opcao = sc.nextLine().trim();
            System.out.println();

            switch (opcao) {
                case "1":
                    System.out.print("  Digite o nome (parcial): ");
                    buscarPorNome(escala, sc.nextLine());
                    break;
                case "2":
                    System.out.print("  Digite a data (ex: 19/06 ou 19/06/2026): ");
                    buscarPorData(escala, sc.nextLine());
                    break;
                case "0":
                    limpar();
                    System.out.println("\n  Até logo! ☕\n");
                    return;
                default:
                    System.out.println("  ✗  Opção inválida.\n");
            }

            System.out.print("  Pressione ENTER para voltar ao menu...");
            sc.nextLine();
        }
    }
}
