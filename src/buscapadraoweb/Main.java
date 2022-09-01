//compile: javac -d build/classes src/buscaweb/*.java src/buscapadraoweb/*.java
//run: java -cp build/classes buscapadraoweb/Main
//compile-and-run: javac -d build/classes src/buscaweb/*.java src/buscapadraoweb/*.java && java -cp build/classes buscapadraoweb/Main
//args: "http://www.mcce.org.br/newsletter" "https://www.univali.br/" "https://www.navship.com.br/"

package buscapadraoweb;

import buscaweb.CapturaRecursosWeb;
import java.util.ArrayList;

public class Main {
	
    // busca caracter em vetor e retorna indice
	public static int get_char_ref (char[] vet, char ref ){
        for (int i=0; i<vet.length; i++ ){
            if (vet[i] == ref){
                return i;
            }
        }
        return -1;
    }

    // busca string em vetor e retorna indice
    public static int get_string_ref (String[] vet, String ref ){
        for (int i=0; i<vet.length; i++ ){
            if (vet[i].equals(ref)){
                return i;
            }
        }
        return -1;
    }

    

    //retorna o próximo estado, dado o estado atual e o símbolo lido
    public static int proximo_estado(char[] alfabeto, int[][] matriz,int estado_atual,char simbolo){
        int simbol_indice = get_char_ref(alfabeto, simbolo);
        if (simbol_indice != -1){
            return matriz[estado_atual][simbol_indice];
        }else{
            return -1;
        }
    }

    /*
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length > 0){
            try{

                for(String arg : args){
                    System.out.println("\nIniciando Conexao com " + arg);

                    //instancia e usa objeto que captura código-fonte de páginas Web
                    CapturaRecursosWeb crw = new CapturaRecursosWeb();
                    crw.getListaRecursos().add(arg);
                    ArrayList<String> listaCodigos = crw.carregarRecursos();

                    String codigoHTML = listaCodigos.get(0);

                    if(codigoHTML == null){
                        System.out.println("Falha na Conexao!");
                    } else {
                        System.out.println("HTML Obtido");

                        

                        //mapa do alfabeto
                        char[] alfabeto = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ@.-_".toCharArray();

                        //mapa de estados
                        String[] estados = new String[6];
                        estados[0] = "q0";
                        estados[1] = "q1";
                        estados[2] = "q2";
                        estados[3] = "q3";
                        estados[4] = "q4";
                        estados[5] = "q5";

                        String estado_inicial = "q0";

                        //estados finais
                        String[] estados_finais = new String[2];
                        estados_finais[0] = "q4";
                        estados_finais[1] = "q5";

                        //tabela de transição de AFD para reconhecimento números de dois dígitos
                        int[][] matriz = new int[estados.length][alfabeto.length];

                        //transições de q0
                        for(int i=0; i < alfabeto.length; i++) {
                            matriz[get_string_ref(estados, "q0")][get_char_ref(alfabeto, alfabeto[i])] = (alfabeto[i] == '@')
                                ? get_string_ref(estados, "q1")
                                : get_string_ref(estados, "q0");
                        }

                        //transições de q1
                        for(int i=0; i < alfabeto.length; i++) {
                            matriz[get_string_ref(estados, "q1")][get_char_ref(alfabeto, alfabeto[i])] = (alfabeto[i] == '@')
                                ? -1
                                : (alfabeto[i] == '.') 
                                ? get_string_ref(estados, "q2")
                                : get_string_ref(estados, "q1");
                        }

                        //transições de q2
                        for(int i=0; i < alfabeto.length; i++) {
                            matriz[get_string_ref(estados, "q2")][get_char_ref(alfabeto, alfabeto[i])] = (alfabeto[i] == '@' || alfabeto[i] == '-' || alfabeto[i] == '_' || alfabeto[i] == '.')
                                ? -1 
                                : get_string_ref(estados, "q3");
                        }

                        //transições de q3
                        for(int i=0; i < alfabeto.length; i++) {
                            matriz[get_string_ref(estados, "q3")][get_char_ref(alfabeto, alfabeto[i])] = (alfabeto[i] == '@' || alfabeto[i] == '-' || alfabeto[i] == '_' || alfabeto[i] == '.')
                                ? -1 
                                : get_string_ref(estados, "q4");
                        }

                        //transições de q4
                        for(int i=0; i < alfabeto.length; i++) {
                            matriz[get_string_ref(estados, "q4")][get_char_ref(alfabeto, alfabeto[i])] = (alfabeto[i] == '@' || alfabeto[i] == '-' || alfabeto[i] == '_')
                                ? -1 
                                : (alfabeto[i] == '.')
                                ? get_string_ref(estados, "q1")
                                : get_string_ref(estados, "q5");
                        }

                        //transições de q5
                        for(int i=0; i < alfabeto.length; i++) {
                            matriz[get_string_ref(estados, "q5")][get_char_ref(alfabeto, alfabeto[i])] = (alfabeto[i] == '.')
                                ? get_string_ref(estados, "q2")
                                : -1;
                        }

                        
                        int estado = get_string_ref(estados, estado_inicial);
                        int estado_anterior = -1;
                        ArrayList<String> palavras_reconhecidas = new ArrayList<String>();


                        String palavra = "";

                        System.out.println("Iniciando Varredura");

                        //varre o código-fonte de um código
                        for (int i=0; i<codigoHTML.length(); i++){

                            estado_anterior = estado;
                            estado = proximo_estado(alfabeto, matriz, estado, codigoHTML.charAt(i));
                            //se o não há transição
                            if (estado == -1){
                                //pega estado inicial
                                estado = get_string_ref(estados, estado_inicial);
                                // se o estado anterior foi um estado final
                                if (get_string_ref(estados_finais, estados[estado_anterior]) != -1){
                                    //se a palavra não é vazia adiciona palavra reconhecida
                                    if ( ! palavra.equals("")){
                                        palavras_reconhecidas.add(palavra);
                                    }
                                    // se ao analisar este caracter não houve transição
                                    // teste-o novamente, considerando que o estado seja inicial
                                    i--;
                                }
                                //zera palavra
                                palavra = "";
                                
                            } else {
                                //se houver transição válida, adiciona caracter a palavra
                                palavra += codigoHTML.charAt(i);
                            }
                        }

                        System.out.println("Varredura Completa");

                        if(palavras_reconhecidas.isEmpty()){
                            System.out.println("Nenhuma Correspondencia Encontrada");
                        } else{
                            System.out.println("Listando Correspondencias Encontradas em " + arg + ":\n\n");

                            //foreach no Java para exibir todas as palavras reconhecidas
                            for (String p: palavras_reconhecidas){
                                System.out.println (p);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Nenhum argumento passado!");
        }


    }
	
}
