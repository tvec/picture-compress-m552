picture-compress-m552
=====================

Picture Compression Android Project 

Hello! This is a simple Android Project that compares lossy compression with Android's compression I created for 
my Applications of Scientific Computing class.

It uses 2-D Discrete Cosine Transform and quantization to compress images 

Process of Compression:
2.) Scale image to about 200hx200w (Otherwise phone can crash)
3.) After scaling, adjust image to be divisible by 8. 
4.) If grayscale selected (default seleciton), convert to gray.
	Other wise, seperate r,g,b or y,u,v channels from image.
5.) Apply forward DCT (Y=C*X*C_transpose)	on to the image. This breaks up the image into 8x8 matrices and multiply.
-used modular division to break up into 8x8 matrices.
6.) Once all of the image goes through the DCT process, apply quantization.
7.) Then de-quantize. 
8.) Now, reverse the process with reverse DCT (X=C_transpose*Y*C)
9.) Convert the matrices back to color array and create bitmap.
	For RGB and YUV, it needs to re-combine the channels

Future Todo's:
Add Huffman coding
Add option to save images
Add working camera button

You can contact me at:
tvec623@gmail.com
