clear;
clc;
im = imread('lena.jpg');
imshow(im);
watermark = imread('dong.jpg');
watermark = rgb2gray(watermark);
watermark = im2bw(watermark,graythresh(watermark));
[xmark,ymark] = size(watermark);
figure;
imshow(watermark);
tmpim = rgb2ntsc(im);
%im(:,:,1)
tmpim = ceil(tmpim.*256);
tmpim = uint8(tmpim);
%此时tmpim为一个uint8的yiq矩阵
Y = tmpim(:,:,1);
y1 = Y;
y2 = double(Y);
[cA0,cH0,cV0,cD0] = dwt2(Y,'db1');
tmpcA0 = uint16(cA0);
[xcA,~] = size(cA0);
blocksize = floor(xcA/xmark);
delta = 105;

for i = 1:blocksize:xcA
    for j = 1:blocksize:xcA
        [U,S,V] = svd(cA0(i:i+blocksize-1,j:j+blocksize-1));
        [xs,ys] = find(S==max(max(S)));
        w = watermark(ceil(i/blocksize),ceil(j/blocksize));
        ga = floor(S(xs,ys)/delta);
        dd = S(xs,ys)/delta-ga;
        
        if mod(ga+w,2) == 1 && dd<0.5
            S(xs,ys) = (ga-0.5)*delta;
        elseif mod(ga+w,2) == 1 && dd>=0.5
            S(xs,ys) = (ga+1.5)*delta;
        else
            S(xs,ys) = (ga+0.5)*delta;
        end
        cA0(i:i+blocksize-1,j:j+blocksize-1) = U*S*V';
    end
end
Y = idwt2(cA0,cH0,cV0,cD0,'db1');
tmpim(:,:,1) = Y;
%im(:,:,1)
tmpim = ntsc2rgb(tmpim);
%im(:,:,1)
%watermark = reshape(watermark,xmark,[]);
figure;imshow(tmpim)
imwrite(tmpim,'markedImg.bmp','bmp');
%figure;imshow(watermark);