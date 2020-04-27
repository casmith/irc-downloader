import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class QueueService {
  constructor(private http: HttpClient) {
  }

  getQueue() : any {
    return this.http.get('/api/queue');
  }

  enqueue(items: any) : any {
    return this.http.post('/api/queue', items)
      .pipe(
        tap(data => {
          console.log(data)
        }, err => {
          console.log(err)
        })
      );
  }
}
