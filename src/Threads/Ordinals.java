package Threads;

import Stats.CountingThread;
import Stats.ThreadData;

import java.text.DecimalFormat;

public class Ordinals extends CountingThread {
    public Ordinals(ThreadData t) {
        super(t);
    }
    private final String[] tensNames = {
            "",
            " ten",
            " twenty",
            " thirty",
            " forty",
            " fifty",
            " sixty",
            " seventy",
            " eighty",
            " ninety"
    };
    private final String[] tensOrds = {
            "",
            " tenth",
            " twentieth",
            " thirtieth",
            " fortieth",
            " fiftieth",
            " sixtieth",
            " seventieth",
            " eightieth",
            " ninetieth"
    };

    private final String[] numNames = {
            "",
            " one",
            " two",
            " three",
            " four",
            " five",
            " six",
            " seven",
            " eight",
            " nine",
            " ten",
            " eleven",
            " twelve",
            " thirteen",
            " fourteen",
            " fifteen",
            " sixteen",
            " seventeen",
            " eighteen",
            " nineteen"
    };
    private final String[] numOrds = {
            "",
            " first",
            " second",
            " third",
            " fourth",
            " fifth",
            " sixth",
            " seventh",
            " eighth",
            " ninth",
            " tenth",
            " eleventh",
            " twelfth",
            " thirteenth",
            " fourteenth",
            " fifteenth",
            " sixteenth",
            " seventeenth",
            " eighteenth",
            " nineteenth"
    };
    private String convertLessThanOneThousand(int number) {
        String soFar;
        if (number % 100 < 20){
            soFar = numNames[number % 100];
            number /= 100;
        }
        else {
            soFar = numNames[number % 10];
            number /= 10;
            soFar = tensNames[number % 10] + soFar;
            number /= 10;
        }
        if (number == 0) return soFar;
        return numNames[number] + " hundred" + soFar;
    }
    private String convertOrdLessThanOneThousand(int number) {
        String soFar;
        if(number % 100 == 0) {
            return numNames[number/100] + " hundredth";
        }
        else if (number % 100 < 20){
            soFar = numOrds[number % 100];
            number /= 100;
        }
        else if (number % 10 == 0) {
            number /= 10;
            soFar = tensOrds[number % 10];
            number /= 10;
        } else {
            soFar = numOrds[number % 10];
            number /= 10;
            soFar = tensNames[number % 10] + soFar;
            number /= 10;
        }
        if (number == 0) return soFar;
        return numNames[number] + " hundred and" + soFar;
    }
    private String convertOrd(long number) {
        if (number == 0) { return "zeroth"; }
        String snumber;
        String mask = "000000000000";
        DecimalFormat df = new DecimalFormat(mask);
        snumber = df.format(number);
        int billions = Integer.parseInt(snumber.substring(0,3));
        int millions  = Integer.parseInt(snumber.substring(3,6));
        int hundredThousands = Integer.parseInt(snumber.substring(6,9));
        int thousands = Integer.parseInt(snumber.substring(9,12));
        String result = "";
        if(billions != 0) {
            result = convertLessThanOneThousand(billions) + " billion";
            if(number%1000000000!=0) result+=" ";
        }
        if(millions != 0) {
            result += convertLessThanOneThousand(millions) + " million";
            if(number%1000000!=0) result+=" ";
        }
        String tradHundredThousands = switch (hundredThousands) {
            case 0 -> "";
            case 1 -> "one thousand";
            default -> convertLessThanOneThousand(hundredThousands)
                    + " thousand";
        };
        result =  result + tradHundredThousands;
        if(thousands==0) result+="th";
        else {
            if(thousands/10==0) result+=" and";
            String tradThousand = convertOrdLessThanOneThousand(thousands);
            result = result + " " + tradThousand;
        }
        return result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ").trim();
    }
    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return this.convertOrd(num);
    }
}
