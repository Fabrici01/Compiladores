/** Abstração do analisador léxico (a Presenter depende dela, não da implementação). */
interface AnalisadorLexico {
    ResultadoAnalise analisar(String codigoFonte);
}