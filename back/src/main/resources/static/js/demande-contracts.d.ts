type RequiredField = {
    name: string;
    label: string;
};

type FieldContract = {
    type: string;
    required: boolean;
};

type ModelContract = Record<string, FieldContract>;

interface DemandeContracts {
    stepMax: number;
    models: {
        demandeur: ModelContract;
        passport: ModelContract;
        visaTransformable: ModelContract;
        pieceJointe: ModelContract;
        demande: ModelContract;
        demandeDuplicataSansDonnees: ModelContract;
        demandeTransfertSansDonnees: ModelContract;
        demandeDetail: ModelContract;
        demandeListeItem: ModelContract;
    };
    requiredFields: {
        step1: RequiredField[];
        step2: RequiredField[];
        step3: RequiredField[];
        step3Transfer: RequiredField[];
        step4: RequiredField[];
    };
}

declare global {
    interface Window {
        DemandeContracts?: DemandeContracts;
    }
}

export {};