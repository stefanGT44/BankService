export interface IResponse {
    data: string,
    messages: [{
        code: string,
        message: string,
        severity: string
    }];

}
