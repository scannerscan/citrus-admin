import {Injectable} from '@angular/core';
import {Http, Response, Headers, RequestOptions} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {SpringBean} from "../model/springbean";

@Injectable()
export class SpringBeanService {

    constructor (private http: Http) {}

    private _serviceUrl = 'beans';

    searchBeans(type: string) {
        return this.http.post(this._serviceUrl + '/search', type, new RequestOptions({ headers: new Headers({ 'Content-Type': 'plain/text' }) }))
                        .map(res => <string[]> res.json())
                        .catch(this.handleError);
    }

    getBeans(type: string) {
        return this.http.get(this._serviceUrl + '/' + type)
                        .map(res => <SpringBean[]> res.json())
                        .catch(this.handleError);
    }

    getBean(type: string, id: string) {
        return this.http.get(this._serviceUrl + '/' + type + '/' + id)
            .map(res => <SpringBean> res.json())
            .catch(this.handleError);
    }

    createBean(bean: any) {
        return this.http.post(this._serviceUrl, JSON.stringify(bean), new RequestOptions({ headers: new Headers({ 'Content-Type': 'application/json' }) }))
                .catch(this.handleError);
    }

    updateBean(bean: any) {
        return this.http.put(this._serviceUrl + '/' + bean.id, JSON.stringify(bean), new RequestOptions({ headers: new Headers({ 'Content-Type': 'application/json' }) }))
                .catch(this.handleError);
    }

    deleteBean(id: string) {
        return this.http.delete(this._serviceUrl + '/' + id)
                .catch(this.handleError);
    }

    private handleError (error: Response) {
        return Observable.throw(error.json() || 'Server error');
    }

}