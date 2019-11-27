package com.example.gpos700.gui.ModuloVendas;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

import javax.xml.parsers.DocumentBuilderFactory;

import br.com.gertec.gedi.GEDI;
import br.com.gertec.gedi.enums.GEDI_PRNTR_e_Alignment;
import br.com.gertec.gedi.enums.GEDI_PRNTR_e_BarCodeType;
import br.com.gertec.gedi.enums.GEDI_PRNTR_e_Status;
import br.com.gertec.gedi.interfaces.IPRNTR;
import br.com.gertec.gedi.structs.GEDI_PRNTR_st_BarCodeConfig;
import br.com.gertec.gedi.structs.GEDI_PRNTR_st_PictureConfig;
import br.com.gertec.gedi.structs.GEDI_PRNTR_st_StringConfig;

public class AcionarImpressora {

    private IPRNTR printer;

    public AcionarImpressora() {
        printer = GEDI.getInstance().getPRNTR();
    }


    //Produção
    public void imprimirCFe(String content, boolean cancelamento) throws Exception {

        AcionarImpressora acionarImpressora = new AcionarImpressora();
        String retorno = acionarImpressora.printStatus();

        if (!retorno.isEmpty() && retorno.equalsIgnoreCase("Impressora pronta para uso.")) {

            acionarImpressora.printPaperUsage();
            acionarImpressora.printInit();

            if (!cancelamento) montarCFeVenda(acionarImpressora, content);
            else montarCFeCancelamento(acionarImpressora, content);

            acionarImpressora.printOutput();
            acionarImpressora.printPaperReset();
        }

    }


    //Recursos da Impressora (Gertec - GPOS700)
    private void printInit() {
        try {

            printer.Init();

        } catch (Exception e) {
            System.out.println("Erro no Printer Init: " + e.getMessage());
        }
    }

    private void printOutput() {
        try {

            printer.Output();

        } catch (Exception e) {
            System.out.println("Erro no Printer Output: " + e.getMessage());
        }
    }

    private String printStatus() {
        try {
            GEDI_PRNTR_e_Status printerStatus = printer.Status();
            switch (printerStatus) {
                case OK:
                    return "Impressora pronta para uso.";
                case OUT_OF_PAPER:
                    return "A impressora está sem papel ou com tampa aberta.";
                case OVERHEAT:
                    return "A impressora está superaquecida.";
                case UNKNOWN_ERROR:
                    return "Valor padrão para erros não mapeados.";
            }


        } catch (Exception e) {
            System.out.println("Erro ao capurar status da impressora: " + e.getMessage());
        }
        return null;
    }

    private void printPaperUsage() {
        try {
            int paperUsage = printer.GetPaperUsage();
            System.out.println("Utilização de papel: " + paperUsage);

        } catch (Exception ex) {
            System.out.println("Erro ao capturar utilização de papel.");
        }
    }

    private void printPaperReset() {
        try {

            printer.ResetPaperUsage();

        } catch (Exception e) {

            System.out.println("Erro ao resetar utilização de papel da impressora.");
        }
    }

    private void printBarCode(GEDI_PRNTR_e_BarCodeType barCodeType, int width, int height, String content) {
        try {

            GEDI_PRNTR_st_BarCodeConfig config;

            if (barCodeType == GEDI_PRNTR_e_BarCodeType.QR_CODE) {
                config = new GEDI_PRNTR_st_BarCodeConfig();
                config.barCodeType = barCodeType;
                config.height = height;
                config.width = width;
            } else {
                config = new GEDI_PRNTR_st_BarCodeConfig();
                config.barCodeType = barCodeType;
                config.height = height;
                config.width = width;
            }

            printer.DrawBarCode(config, content);

        } catch (Exception e) {
            System.out.println("Erro ao realizar impressão: " + e.getMessage());
        }
    }

    private void printImage(int width, int height, int offset, Resources resources, int image) {
        try {

            GEDI_PRNTR_st_PictureConfig config = new GEDI_PRNTR_st_PictureConfig();
            config.alignment = GEDI_PRNTR_e_Alignment.CENTER;
            config.height = height;
            config.width = width;
            config.offset = offset;

            Bitmap bitmap = BitmapFactory.decodeResource(resources, image);
            Bitmap bitmapReduzido = Bitmap.createScaledBitmap(bitmap, width, height, true);
            printer.DrawPictureExt(config, bitmapReduzido);

        } catch (Exception e) {
            System.out.println("Erro ao imprimir imagem: " + e.getMessage());
        }
    }

    private void printString(String content, Paint paint) {
        try {

            GEDI_PRNTR_st_StringConfig config = new GEDI_PRNTR_st_StringConfig();
            config.lineSpace = 1;
            config.offset = 1;
            config.paint = paint;

            printer.DrawStringExt(config, content);

        } catch (Exception e) {
            System.out.println("Erro ao imprimir String: " + e.getMessage());
        }
    }

    private Paint textConfigurations(int textSize, Typeface typeface) {

        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setTypeface(typeface);
        return paint;
    }

    private void printBlankLine(int height) {
        try {
            printer.DrawBlankLine(height);

        } catch (Exception e) {
            System.out.println("Erro ao imprimir linha vazia: " + e.getMessage());
        }
    }


    //Montar CF-es
    private void montarCFeVenda(AcionarImpressora acionarImpressora, String content) throws Exception {

        System.out.println("Montando CFe para impressão");

        imprimirCabecalho(acionarImpressora, content);
        imprimirCorpo(acionarImpressora, content);
        imprimirRodape(acionarImpressora, content);
    }

    private void montarCFeCancelamento(AcionarImpressora acionarImpressora, String content) throws Exception {

        System.out.println("Montando CFe Cancelamento para impressão");

        imprimirCabecalho(acionarImpressora, content);
        imprimirCorpoCancelamento(acionarImpressora, content);
        imprimirRodapeCancelamento(acionarImpressora, content);

    }


    //Particionando a montagem no CF-e
    private void imprimirCabecalho(AcionarImpressora acionarImpressora, String content) throws Exception {

        Paint paint = acionarImpressora.textConfigurations(17, Typeface.DEFAULT);

        //Fantansia
        String nomeFantasia = getElementValue("xFant", content, 0);

        //Razão Social
        String razaoSocial = getElementValue("xNome", content, 0);

        //Endereço
        String logradouro = getElementValue("xLgr", content, 0);
        String complemento = getElementValue("xCpl", content, 0);
        String numero = getElementValue("nro", content, 0);
        String bairro = getElementValue("xBairro", content, 0);
        String municipio = getElementValue("xMun", content, 0);
        String cep = getElementValue("CEP", content, 0);
        String uf = getElementValue("cUF", content, 0);

        //Informações
        String cnpj = getElementValue("CNPJ", content, 0);
        String inscricaoEstadual = getElementValue("IE", content, 0);
        String inscricaoMunicipal = getElementValue("IM", content, 0);




        if (!nomeFantasia.isEmpty() && !nomeFantasia.equalsIgnoreCase("Nao Informado")){
            paint.setTextAlign(Paint.Align.CENTER);
            acionarImpressora.printString(nomeFantasia, paint);
        }

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString(razaoSocial, paint);

        if (!complemento.isEmpty() && !complemento.equalsIgnoreCase("Nao Informado")) {
            paint.setTextAlign(Paint.Align.CENTER);
            acionarImpressora.printString(complemento, paint);
        }

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString(logradouro + ", " + numero, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString(municipio + ", " + bairro + ", " + Estados.getSigla(Integer.parseInt(uf)), paint);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("CEP: " + formatarCep(cep) + " CNPJ: " + formatarCnpj(cnpj), paint);

        if (!inscricaoEstadual.isEmpty() && inscricaoMunicipal.isEmpty()) {
            paint.setTextAlign(Paint.Align.CENTER);
            acionarImpressora.printString("IE:" + formatarIE(inscricaoEstadual), paint);
        }

        if (inscricaoEstadual.isEmpty() && !inscricaoMunicipal.isEmpty()) {
            paint.setTextAlign(Paint.Align.CENTER);
            acionarImpressora.printString("IM:" + formatarIM(inscricaoMunicipal), paint);
        }

        if (!inscricaoEstadual.isEmpty() && !inscricaoMunicipal.isEmpty()) {
            paint.setTextAlign(Paint.Align.CENTER);
            acionarImpressora.printString("IE:" + formatarIE(inscricaoEstadual) + " IM:" + formatarIM(inscricaoMunicipal), paint);
        }

        Paint line = acionarImpressora.textConfigurations(17, Typeface.DEFAULT_BOLD);
        line.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("______________________________________", line);

    }

    private void imprimirRodape(AcionarImpressora acionarImpressora, String content) throws Exception{

        //Numero Serie SAT
        String numeroSerieSat = getElementValue("nserieSAT", content, 0);

        //Data e hora emissãop
        String dataEmissao = getElementValue("dEmi", content, 0);
        String horaEmissao = getElementValue("hEmi", content, 0);

        //Chave CF-e
        String chaveCFe = getAttributeValue("infCFe", "Id", content, 0);



        Paint line = acionarImpressora.textConfigurations(17, Typeface.DEFAULT_BOLD);
        line.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("______________________________________", line);

        Paint paintBold = acionarImpressora.textConfigurations(17, Typeface.DEFAULT_BOLD);
        paintBold.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("SAT No. " + numeroSerieSat, paintBold);

        Paint paint = acionarImpressora.textConfigurations(17, Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString(formatarDataHora(dataEmissao, horaEmissao), paint);

        acionarImpressora.printBlankLine(10);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString(formatarChave(chaveCFe.replace("CFe", "")), paint);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printBlankLine(20);
        acionarImpressora.printBarCode(GEDI_PRNTR_e_BarCodeType.CODE_128, 800, 100, chaveCFe.replace("CFe", ""));

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printBarCode(GEDI_PRNTR_e_BarCodeType.QR_CODE, 400, 400, chaveCFe.replace("CFe", ""));

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printBlankLine(10);
        acionarImpressora.printString("Consulta o QR Code pelo aplicativo", paint);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("'De olho na nota'", paint);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("disponível na AppStore (Apple) e ", paint);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("PlayStore (Android).", paint);

    }

    private void imprimirRodapeCancelamento(AcionarImpressora acionarImpressora, String content) throws Exception {

        //Numero Serie SAT
        String numeroSerieSat = getElementValue("nserieSAT", content, 0);

        //Data e hora emissãop
        String dataEmissao = getElementValue("dEmi", content, 0);
        String horaEmissao = getElementValue("hEmi", content, 0);

        //Chave CF-e
        String chaveCFe = getAttributeValue("infCFe", "Id", content, 0);

        Paint paintBold = acionarImpressora.textConfigurations(17, Typeface.DEFAULT_BOLD);
        paintBold.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("SAT No. " + numeroSerieSat, paintBold);

        Paint paint = acionarImpressora.textConfigurations(17, Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString(formatarDataHora(dataEmissao, horaEmissao), paint);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printBlankLine(10);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString(formatarChave(chaveCFe.replace("CFe", "")), paint);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printBlankLine(10);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printBlankLine(10);
        acionarImpressora.printBarCode(GEDI_PRNTR_e_BarCodeType.CODE_128, 800, 100, chaveCFe.replace("CFe", ""));

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printBarCode(GEDI_PRNTR_e_BarCodeType.QR_CODE, 400, 400, chaveCFe.replace("CFe", ""));

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printBlankLine(10);
        acionarImpressora.printString("Consulta o QR Code pelo aplicativo", paint);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("'De olho na nota'", paint);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("disponível na AppStore (Apple) e ", paint);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("PlayStore (Android).", paint);

    }

    private void imprimirCorpo(AcionarImpressora acionarImpressora, String content) throws Exception{

        //Informações cupom fiscal
        String numeroCFe = getElementValue("nCFe", content, 0);

        Paint line = acionarImpressora.textConfigurations(17, Typeface.DEFAULT_BOLD);
        line.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("Extrato " + numeroCFe, line);

        line.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("CUPOM FISCAL ELETRÔNICO - SAT", line);

        line.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("______________________________________", line);


        //Informações de Destinatário
        if (content.contains("<dest>")) {

            int begin = content.indexOf("<dest>");
            int end = content.lastIndexOf("</dest>");

            String dest = content.substring(begin, end);
            dest = dest.concat("</dest>");
            System.out.println(dest);

            Paint paint = acionarImpressora.textConfigurations(17, Typeface.DEFAULT);

            if (dest.contains("<CPF>")) {

                String cpf = getElementValue("CPF", dest, 0);

                paint.setTextAlign(Paint.Align.CENTER);
                acionarImpressora.printString("CPF/CNPJ do Consumidor: " + formatarCpf(cpf), paint);

            } else if (dest.contains("<CNPJ>")) {

                String cnpj = getElementValue("CNPJ", dest, 0);

                paint.setTextAlign(Paint.Align.CENTER);
                acionarImpressora.printString("CPF/CNPJ do Consumidor: " + formatarCnpj(cnpj), paint);

            } else {

                paint.setTextAlign(Paint.Align.CENTER);
                acionarImpressora.printString("CPF/CNPJ do Consumidor: Não Informado", paint);
            }


            if (dest.contains("<xNome>")) {

                String consumidor = getElementValue("xNome", dest, 0);

                paint.setTextAlign(Paint.Align.CENTER);
                acionarImpressora.printString("Razão Social/Nome: " + consumidor, paint);

            } else {

                paint.setTextAlign(Paint.Align.CENTER);
                acionarImpressora.printString("Razão Social/Nome: Não Informado", paint);
            }


            paint.setTextAlign(Paint.Align.CENTER);
            acionarImpressora.printString("______________________________________", line);

        } else if (content.contains("<dest/>")) {

            Paint paint = acionarImpressora.textConfigurations(17, Typeface.DEFAULT);

            paint.setTextAlign(Paint.Align.CENTER);
            acionarImpressora.printString("CPF/CNPJ do Consumidor: Não Informado", paint);

            paint.setTextAlign(Paint.Align.CENTER);
            acionarImpressora.printString("Razão Social/Nome: Não Informado", paint);

            paint.setTextAlign(Paint.Align.CENTER);
            acionarImpressora.printString("______________________________________", line);

        }


        //Descrição dos produtos
        Paint paint = acionarImpressora.textConfigurations(17, Typeface.DEFAULT);
        acionarImpressora.printString("#|COD|DESC|QTD|UN|VL UN R$|(VL TR R$)*|VL ITEM R$", paint);

        paint.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("______________________________________", line);



        //Produtos
        int dets = getElementLength("det", content);
        Paint itens = acionarImpressora.textConfigurations(17, Typeface.DEFAULT);

        for (int i=0; i < dets; i++) {

            String numeroItem = getAttributeValue("det", "nItem", content, i);
            String codigo = getElementValue("cProd", content, i);
            String descricao = getElementValue("xProd", content, i);
            String quantidade = getElementValue("qCom", content, i);
            String unidadeMedida = getElementValue("uCom", content, i);
            String valorUnidade = getElementValue("vUnCom", content, i);
            String valorProduto = getElementValue("vProd", content, i);

            //First line
            itens.setTextAlign(Paint.Align.LEFT);
            String firstLine = numeroItem + " " + codigo + " " + descricao;
            if (firstLine.length() > 40) firstLine = firstLine.substring(0,40);
            acionarImpressora.printString(firstLine, itens);

            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String valueFormated = decimalFormat.format(Double.parseDouble(valorUnidade));

            //Second line
            itens.setTextAlign(Paint.Align.LEFT);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(quantidade.replace(".0000", "")).append(" ").append(unidadeMedida).append(" x ").append("R$ ").append(valueFormated.replace(",", "."));
            int lengh = 60 - valorProduto.length();
            for (int j=stringBuilder.length(); j <= lengh; j++) stringBuilder.append(" ");
            stringBuilder.append("R$ ").append(valorProduto);


            acionarImpressora.printString(stringBuilder.toString(), itens);
            acionarImpressora.printBlankLine(8);
        }


        //Totais
        String brutoTotal = getLastElementValue("vProd", content);
        String descontos = getElementValue("vDescSubtot", content, 0);
        String acrescimos = getElementValue("vAcresSubtot", content, 0);
        String total = getElementValue("vCFe", content, 0);

        paint.setTextAlign(Paint.Align.LEFT);
        acionarImpressora.printString("Total bruto dos itens                                " + "R$ " + brutoTotal, itens);

        if (!descontos.isEmpty()) {
            paint.setTextAlign(Paint.Align.LEFT);
            acionarImpressora.printString("Descontos sobre subtotal                      " + "R$ " + descontos, itens);
        }

        if (!acrescimos.isEmpty()) {
            paint.setTextAlign(Paint.Align.LEFT);
            acionarImpressora.printString("Acréscimo sobre subtotal                      " + "R$ " + descontos, itens);
        }

        paint.setTextAlign(Paint.Align.LEFT);
        acionarImpressora.printString("TOTAL                                                          " + "R$ " + total, line);
        printBlankLine(7);



        //Pagamento
        int meiosPagamento = getElementLength("MP", content);
        if (meiosPagamento != 0){
            for (int i=0; i < meiosPagamento; i++) {
                String codigoMeioPagamento = getElementValue("cMP", content, i);
                String valorMeioPagamento = getElementValue("vMP", content, i);
                valorMeioPagamento = "R$ " + valorMeioPagamento;

                String pagamento = MeioPagamento.getMeioPagamento(Integer.parseInt(codigoMeioPagamento));
                int resultado = 72 - ( pagamento.length() + valorMeioPagamento.length() );

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(pagamento);

                for (int j=0; j<resultado; j++) stringBuilder.append(" ");

                stringBuilder.append(valorMeioPagamento);
                printString(stringBuilder.toString(), itens);
            }
        }

        String valorTroco = getElementValue("vTroco", content, 0);
        printString("Troco                                                             " + "R$ " + valorTroco, itens);
        printBlankLine(7);


        //Observações Fisco e Lei Transparencia
        int observacoesFisco = getElementLength("obsFisco", content);
        if (observacoesFisco != 0){
            for (int i=0; i < observacoesFisco; i++){

                String obsFisco = getAttributeValue("obsFisco", "xCampo", content, i);
                String mensagemFisco = getElementValue("xTexto", content, i);

                line.setTextAlign(Paint.Align.CENTER);
                printString(obsFisco + "   " + mensagemFisco, line);
            }
        }

        String lei12741 = getElementValue("vCFeLei12741", content, 0);
        if (!lei12741.isEmpty()){
            paint.setTextAlign(Paint.Align.CENTER);
            printString("Lei 12.741 (Transparência): R$ " + lei12741, paint);
        }

    }

    private void imprimirCorpoCancelamento(AcionarImpressora acionarImpressora, String content) throws Exception{


        //Informações cupom fiscal
        String numeroCFe = getElementValue("nCFe", content, 0);

        Paint line = acionarImpressora.textConfigurations(17, Typeface.DEFAULT_BOLD);
        line.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("Extrato " + numeroCFe, line);

        line.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("CUPOM FISCAL ELETRÔNICO - SAT", line);

        line.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("CANCELAMENTO", line);

        line.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("______________________________________", line);

        line.setTextAlign(Paint.Align.CENTER);
        acionarImpressora.printString("DADOS DO CUPOM FISCAL ELETRÔNICO CANCELADO", line);

        printBlankLine(10);


        //Informações de Destinatário
        if (content.contains("<dest>")) {

            int begin = content.indexOf("<dest>");
            int end = content.lastIndexOf("</dest>");

            String dest = content.substring(begin, end);
            dest = dest.concat("</dest>");
            System.out.println(dest);

            Paint paint = acionarImpressora.textConfigurations(17, Typeface.DEFAULT);

            if (dest.contains("<CPF>")) {

                String cpf = getElementValue("CPF", dest, 0);

                paint.setTextAlign(Paint.Align.LEFT);
                acionarImpressora.printString("CPF/CNPJ do Consumidor: " + formatarCpf(cpf), paint);

            } else if (dest.contains("<CNPJ>")) {

                String cnpj = getElementValue("CNPJ", dest, 0);

                paint.setTextAlign(Paint.Align.LEFT);
                acionarImpressora.printString("CPF/CNPJ do Consumidor: " + formatarCnpj(cnpj), paint);

            } else {

                paint.setTextAlign(Paint.Align.LEFT);
                acionarImpressora.printString("CPF/CNPJ do Consumidor: Não Informado", paint);
            }

        } else if (content.contains("<dest/>")) {

            Paint paint = acionarImpressora.textConfigurations(17, Typeface.DEFAULT);

            paint.setTextAlign(Paint.Align.LEFT);
            acionarImpressora.printString("CPF/CNPJ do Consumidor: Não Informado", paint);

            paint.setTextAlign(Paint.Align.LEFT);
            acionarImpressora.printString("Razão Social/Nome: Não Informado", paint);

        }


        String total = getElementValue("vCFe", content, 0);

        Paint paint = acionarImpressora.textConfigurations(17, Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.LEFT);
        acionarImpressora.printString("TOTAL R$ " + total, paint);

        printBlankLine(10);

    }


    //Captura das TAGS xml
    private int getElementLength(String tagName, String content) {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document document;

        try {

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            document = documentBuilderFactory.newDocumentBuilder().parse(byteArrayInputStream);
            NodeList nodeList = document.getElementsByTagName(tagName);
            return nodeList.getLength();

        } catch (Exception e) {
            System.err.println("Erro ao capturar a TAG: " + tagName + ". Mensagem do erro:" + e.getMessage());
            return 0;
        }
    }

    private static String getElementValue(String tagName, String content, int position) {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document document;

        try {

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            document = documentBuilderFactory.newDocumentBuilder().parse(byteArrayInputStream);
            NodeList nodeList = document.getElementsByTagName(tagName);

            if (nodeList.getLength() > 0){

                Element element = (Element) nodeList.item(position);
                return element.getTextContent();

            } else { return ""; }

        } catch (Exception e) {
            System.err.println("Erro ao capturar a TAG: " + tagName + ". Mensagem do erro:" + e.getMessage());
            return "";
        }
    }

    private static String getLastElementValue(String tagName, String content) {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document document;

        try {

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            document = documentBuilderFactory.newDocumentBuilder().parse(byteArrayInputStream);
            NodeList nodeList = document.getElementsByTagName(tagName);


            if (nodeList.getLength() > 0){

                Element element = (Element) nodeList.item(nodeList.getLength() - 1);
                return element.getTextContent();

            } else { return ""; }

        } catch (Exception e) {
            System.err.println("Erro ao capturar a TAG: " + tagName + ". Mensagem do erro:" + e.getMessage());
            return "";
        }
    }

    private static String getAttributeValue(String tagName, String attributeName, String content, int position) {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document document;

        try {

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            document = documentBuilderFactory.newDocumentBuilder().parse(byteArrayInputStream);
            NodeList nodeList = document.getElementsByTagName(tagName);

            if (nodeList.getLength() > 0){

                Element element = (Element) nodeList.item(position);
                return element.getAttribute(attributeName);

            } else { return ""; }

        } catch (Exception e) {
            System.err.println("Erro ao capturar a atributo: " + attributeName + " da tag: " + tagName + ". Mensagem do erro:" + e.getMessage());
            return "";
        }
    }


    //Formatações
    private String formatarCnpj(String content) {

        String bloco1 = content.substring(0, 2);
        String bloco2 = content.substring(2, 5);
        String bloco3 = content.substring(5, 8);
        String bloco4 = content.substring(8, 12);
        String bloco5 = content.substring(12,14);

        content = bloco1 + "." + bloco2 + "." + bloco3 + "/" + bloco4 + "-" + bloco5;

        return content;
    }

    private String formatarCpf(String content) {

        String bloco1 = content.substring(0, 3);
        String bloco2 = content.substring(3, 6);
        String bloco3 = content.substring(6, 9);
        String bloco4 = content.substring(9, 11);

        content = bloco1 + "." + bloco2 + "." + bloco3 + "-" + bloco4;

        return content;
    }

    private String formatarCep(String content) {

        String bloco1 = content.substring(0, 5);
        String bloco2 = content.substring(5, 8);

        content = bloco1 + "-" + bloco2;

        return content;
    }

    private String formatarIE(String content) {

        String bloco1 = content.substring(0, 3);
        String bloco2 = content.substring(3, 6);
        String bloco3 = content.substring(6, 9);
        String bloco4 = content.substring(9, 12);

        content = bloco1 + "." + bloco2 + "." + bloco3 + "." + bloco4;

        return content;
    }

    private String formatarIM(String content) {

        String bloco1 = content.substring(0, 4);
        String bloco2 = content.substring(4, 7);
        String bloco3 = content.substring(7, 10);
        String bloco4 = content.substring(10, 13);

        content = bloco1 + "." + bloco2 + "." + bloco3 + "." + bloco4;

        return content;
    }

    private String formatarDataHora(String dEmi, String hEmi) {

        String ano = dEmi.substring(0, 4);
        String mes = dEmi.substring(4, 6);
        String dia = dEmi.substring(6, 8);

        String hora = hEmi.substring(0, 2);
        String minuto = hEmi.substring(2, 4);
        String segundo = hEmi.substring(4, 6);

        return dia + "/" + mes + "/" + ano + " - " + hora + ":" + minuto + ":" + segundo;
    }

    private String formatarChave(String chaveCFe) {

        StringBuilder stringBuilder = new StringBuilder();
        int aux = 0;

        for (int i = 0; i <= 44; i++) {

            if (i != 0 && i % 4 == 0) {

                stringBuilder.append(chaveCFe.substring(aux,i)).append(" ");
                aux = i;
            }
        }

        return stringBuilder.toString();
    }



}
