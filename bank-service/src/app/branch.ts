import { Bank } from './bank';

export class Branch {

    id: number;
    bank: Bank;
    name: string;
    validFrom: Date;
    validTo: Date;
    swiftCode: string;
    branchCode: string;

}