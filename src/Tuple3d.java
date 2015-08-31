public  class Tuple3d{

   double x, y, z,
          length;

  Tuple3d(){
    x = y = z =0;
  }

  Tuple3d(Tuple3d p){
    this.x = p.x;
    this.y = p.y;
    this.z = p.z;
  }

  Tuple3d(double x, double y, double z){
    this.x = x;
    this.y = y;
    this.z = z;
  }

  void set(double x, double y, double z){
    this.x = x;
    this.y = y;
    this.z = z;
  }

  void set(Tuple3d vec){
    this.x = vec.x;
    this.y = vec.y;
    this.z = vec.z;
  }

  void cross(Tuple3d u,Tuple3d v){
    this.x =  (u.y*v.z) - (u.z*v.y);
    this.y =  (u.z*v.x) - (u.x*v.z);
    this.z =  (u.x*v.y) - (u.y*v.x);
  }

  double Dot(Tuple3d u){
    return  u.x*this.x +
            u.y*this.y +
            u.z*this.z;
  }

  void add(Tuple3d u,Tuple3d v){
    x =  u.x + v.x;
    y =  u.y + v.y;
    z =  u.z + v.z;
  }

  void scaleAdd(double d,Tuple3d u,Tuple3d v){
    x = d*u.x + v.x;
    y = d*u.y + v.y;
    z = d*u.z + v.z;
  }

  void scaleAdd(double d,Tuple3d u){
    x = d*u.x;
    y = d*u.y;
    z = d*u.z;
  }

  void add(Tuple3d u){
    x +=  u.x;
    y +=  u.y;
    z +=  u.z;
  }

  void add(double a){
    x += a;
    y += a;
    z += a;
  }

  void sub(Tuple3d u,Tuple3d v){
    x =  u.x - v.x;
    y =  u.y - v.y;
    z =  u.z - v.z;
  }

  void sub(Tuple3d u){
    x -=  u.x;
    y -=  u.y;
    z -=  u.z;
  }

  void scale(double mul){
    x*=   mul;
    y*=   mul;
    z*=   mul;
  }

  void mul(Tuple3d v1,Tuple3d v2){
    x =   v1.x*v2.x;
    y =   v1.y*v2.y;
    z =   v1.z*v2.z;
  }

  void normalize(){
    length =  Math.sqrt(x*x + y*y + z*z);
    if(length!=0){
      x/=length;
      y/=length;
      z/=length;
    }
  }

  double distance(Tuple3d u){
    return  Math.sqrt((this.x - u.x)*(this.x - u.x) +
                      (this.y - u.y)*(this.y - u.y) +
                      (this.z - u.z)*(this.z - u.z));
  }

  double length(){
    return length =  Math.sqrt(x*x + y*y + z*z);
  }

  void negate(){
    x = - x;
    y = - y;
    z = - z;
  }
}
